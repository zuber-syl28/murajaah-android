package com.sylergy.murajaah.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.database.entity.FriendEntity
import com.sylergy.murajaah.friends.model.FriendRules
import com.sylergy.murajaah.friends.repository.FriendsRepository
import com.sylergy.murajaah.friends.scanner.JoinUrlParser
import com.sylergy.murajaah.network.model.InviteDto
import com.sylergy.murajaah.network.model.InvitePreviewDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FriendsUiState(
    val requests: List<FriendEntity> = emptyList(),
    val friends: List<FriendEntity> = emptyList(),
    val isLoading: Boolean = false,
    val invite: InviteDto? = null,
    val showInviteSheet: Boolean = false,
    val showScanDialog: Boolean = false,
    val joinPreview: InvitePreviewDto? = null,
    val joinError: String? = null,
    val joinSuccess: String? = null
)

private data class TransientState(
    val isLoading: Boolean = false,
    val invite: InviteDto? = null,
    val showInviteSheet: Boolean = false,
    val showScanDialog: Boolean = false,
    val joinPreview: InvitePreviewDto? = null,
    val joinError: String? = null,
    val joinSuccess: String? = null
)

class FriendsViewModel(
    private val repository: FriendsRepository
) : ViewModel() {

    private val _transientState = MutableStateFlow(TransientState())

    val uiState: StateFlow<FriendsUiState> = combine(
        repository.friendsFlow,
        _transientState
    ) { allFriends, transient ->
        val requests = allFriends.filter { it.reviewsYouStatus == "pending" }
        // Exclude pending only requests from regular friends list
        val listed = allFriends
            .filterNot { it.reviewsYouStatus == "pending" && it.youReviewStatus == null }
            .sortedWith(
                compareByDescending<FriendEntity> { FriendRules.dueFor(it) }
                    .thenBy { it.name }
            )
        FriendsUiState(
            requests = requests,
            friends = listed,
            isLoading = transient.isLoading,
            invite = transient.invite,
            showInviteSheet = transient.showInviteSheet,
            showScanDialog = transient.showScanDialog,
            joinPreview = transient.joinPreview,
            joinError = transient.joinError,
            joinSuccess = transient.joinSuccess
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FriendsUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            repository.seedInitialCompanionsIfEmpty()
            repository.refreshFriends()
        }
    }

    fun openInviteSheet() {
        viewModelScope.launch {
            _transientState.value = _transientState.value.copy(showInviteSheet = true)
            if (_transientState.value.invite == null) {
                val result = repository.createInvite()
                result.onSuccess { inviteDto ->
                    _transientState.value = _transientState.value.copy(invite = inviteDto)
                }
            }
        }
    }

    fun closeInviteSheet() {
        _transientState.value = _transientState.value.copy(showInviteSheet = false)
    }

    fun openScanDialog() {
        _transientState.value = _transientState.value.copy(
            showScanDialog = true,
            joinError = null,
            joinPreview = null
        )
    }

    fun closeScanDialog() {
        _transientState.value = _transientState.value.copy(showScanDialog = false)
    }

    fun onScanCode(rawCode: String) {
        val token = JoinUrlParser.parseToken(rawCode)
        if (token.isNullOrBlank()) {
            _transientState.value = _transientState.value.copy(joinError = "Unrecognized QR code or link")
            return
        }
        loadJoinPreview(token)
    }

    fun loadJoinPreview(token: String) {
        viewModelScope.launch {
            _transientState.value = _transientState.value.copy(isLoading = true, joinError = null)
            val result = repository.getInvitePreview(token)
            result.onSuccess { preview ->
                _transientState.value = _transientState.value.copy(joinPreview = preview, isLoading = false)
            }.onFailure {
                _transientState.value = _transientState.value.copy(joinError = "Invite expired or not found", isLoading = false)
            }
        }
    }

    fun acceptInvite(token: String) {
        viewModelScope.launch {
            _transientState.value = _transientState.value.copy(isLoading = true, joinError = null)
            val result = repository.acceptInvite(token)
            result.onSuccess { friendName ->
                _transientState.value = _transientState.value.copy(
                    joinSuccess = friendName,
                    joinPreview = null,
                    showScanDialog = false,
                    isLoading = false
                )
            }.onFailure {
                _transientState.value = _transientState.value.copy(
                    joinError = "Failed to accept invite",
                    isLoading = false
                )
            }
        }
    }

    fun clearJoinSuccess() {
        _transientState.value = _transientState.value.copy(joinSuccess = null)
    }

    fun approveRequest(friend: FriendEntity) {
        viewModelScope.launch {
            repository.approvePartnership(friend)
        }
    }

    fun declineRequest(friend: FriendEntity) {
        viewModelScope.launch {
            repository.declinePartnership(friend)
        }
    }

    fun updateRole(friend: FriendEntity, role: String) {
        viewModelScope.launch {
            repository.updateRole(friend, role)
        }
    }

    fun updateRemind(friend: FriendEntity, remind: Boolean) {
        viewModelScope.launch {
            repository.updateRemind(friend, remind)
        }
    }

    fun stopReviewing(friend: FriendEntity) {
        viewModelScope.launch {
            repository.stopReviewing(friend)
        }
    }

    class Factory(private val repository: FriendsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FriendsViewModel(repository) as T
        }
    }
}

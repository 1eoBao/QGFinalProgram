import request from '../utils/request'

export function searchUser(keyword) {
  return request.get('/friend/search', { params: { keyword } })
}

export function sendFriendApply(toUserId) {
  return request.post('/friend/apply', null, { params: { toUserId } })
}

export function handleFriendApply(id, data) {
  return request.put(`/friend/apply/${id}`, data)
}

export function getFriendApplyList() {
  return request.get('/friend/apply/list')
}

export function getFriendList(groupId) {
  return request.get('/friend/list', { params: { groupId } })
}

export function createFriendGroup(data) {
  return request.post('/friend/group', data)
}

export function moveFriendToGroup(friendId, groupId) {
  return request.put(`/friend/${friendId}/group`, null, { params: { groupId } })
}

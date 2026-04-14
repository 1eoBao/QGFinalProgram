import request from '../utils/request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateUserInfo(data) {
  return request.put('/user/info', data)
}

export function changePassword(data) {
  return request.put('/user/password', data)
}

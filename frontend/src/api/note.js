import request from '../utils/request'

export function createNote(data) {
  return request.post('/note', data)
}

export function getNoteList(params) {
  return request.get('/note/list', { params })
}

export function getNoteDetail(id) {
  return request.get(`/note/${id}`)
}

export function updateNote(id, data) {
  return request.put(`/note/${id}`, data)
}

export function deleteNote(id) {
  return request.delete(`/note/${id}`)
}

export function updateNotePermission(id, data) {
  return request.put(`/note/${id}/permission`, data)
}

export function getNoteHistory() {
  return request.get('/note/history')
}

export function getPublicNote(id) {
  return request.get(`/note/public/${id}`)
}

export function getSharedNotes(params) {
  return request.get('/note/shared', { params })
}

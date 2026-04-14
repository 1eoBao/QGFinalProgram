import request from '../utils/request'

export function analyzeNote(noteId) {
  return request.post(`/ai/analyze/${noteId}`)
}

export function getAnalysis(noteId) {
  return request.get(`/ai/analysis/${noteId}`)
}

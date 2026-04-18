import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/notes',
    children: [
      { path: 'notes', name: 'NoteList', component: () => import('../views/NoteList.vue') },
      { path: 'shared', name: 'SharedNotes', component: () => import('../views/SharedNotes.vue') },
      { path: 'note/create', name: 'NoteCreate', component: () => import('../views/NoteEdit.vue') },
      { path: 'note/:id', name: 'NoteDetail', component: () => import('../views/NoteDetail.vue') },
      { path: 'note/:id/edit', name: 'NoteEdit', component: () => import('../views/NoteEdit.vue') },
      { path: 'friends', name: 'Friends', component: () => import('../views/Friends.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') },
      { path: 'history', name: 'History', component: () => import('../views/History.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPages = ['/login', '/register']
  const isNoteDetail = to.path.startsWith('/note/') && !to.path.endsWith('/edit') && !to.path.endsWith('/create')
  
  if (!publicPages.includes(to.path) && !isNoteDetail && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router

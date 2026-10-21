// Wszystkie rozmowy z backendem w jednym miejscu. Komponenty nie znają adresów ani nagłówków.

const TOKEN_KEY = 'homeapp.token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function saveToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

// Błąd 401 = token wygasł albo jest zły. Osobna klasa, żeby App wiedziała, że trzeba wylogować.
export class UnauthorizedError extends Error {}

async function request(path, options = {}) {
  const headers = { ...options.headers }
  const token = getToken()
  if (token) {
    // Ten sam nagłówek, który ustawiałeś w Postmanie (zakładka Authorization → Bearer Token).
    headers.Authorization = `Bearer ${token}`
  }
  if (options.body) {
    headers['Content-Type'] = 'application/json'
  }

  const response = await fetch(path, { ...options, headers })

  // 401 z tokenem = token wygasł albo jest zły. (401 bez tokenu to np. złe hasło przy logowaniu,
  // a ten błąd ma swój komunikat z backendu, więc obsługuje go kod niżej.)
  if (response.status === 401 && token) {
    throw new UnauthorizedError('Sesja wygasła, zaloguj się ponownie.')
  }
  if (!response.ok) {
    // Nasze serwisy zwracają błędy w formacie ProblemDetail: { "detail": "...", "status": ... }.
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? `Błąd ${response.status}`)
  }
  if (response.status === 204) {
    return null
  }
  return response.json()
}

export async function login(email, password) {
  const result = await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
  saveToken(result.accessToken)
}

export function getMe() {
  return request('/api/auth/me')
}

export function getMyPlan(weekStart) {
  return request(`/api/planner/plans/${weekStart}/me`)
}

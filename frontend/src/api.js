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

    let response
    try {
      response = await fetch(path, { ...options, headers })
    } catch {
      // fetch rzuca wyjątek tylko wtedy, gdy w ogóle nie dostał odpowiedzi: brak sieci albo serwer leży.
      throw new Error('Brak połączenia z serwerem. Sprawdź internet i spróbuj ponownie.')
    }

  // 401 z tokenem = token wygasł albo jest zły. (401 bez tokenu to np. złe hasło przy logowaniu,
  // a ten błąd ma swój komunikat z backendu, więc obsługuje go kod niżej.)
  if (response.status === 401 && token) {
    throw new UnauthorizedError('Sesja wygasła, zaloguj się ponownie.')
  }
  // 403 = jesteś zalogowany, ale ta akcja jest tylko dla rodzica (hasRole("ADMIN") w SecurityConfig).
  if (response.status === 403) {
    throw new Error('Tę akcję może wykonać tylko rodzic.')
  }
  if (!response.ok) {
    // Nasze serwisy zwracają błędy w formacie ProblemDetail: { "detail": "...", "status": ... }.
    // Przy błędzie walidacji jest też "errors": { "firstName": "Imię jest wymagane", ... }.
    const problem = await response.json().catch(() => null)
    const fieldErrors = problem?.errors ? Object.values(problem.errors).join(', ') : null
    throw new Error(fieldErrors ?? problem?.detail ?? `Błąd ${response.status}`)
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

// ----- Panel rodzica (Krok 13a) -----

export function getFamilyPlan(weekStart) {
  return request(`/api/planner/plans/${weekStart}`)
}

export function generatePlan(weekStart) {
  return request(`/api/planner/plans/${weekStart}`, { method: 'POST' })
}

export function deletePlan(weekStart) {
  return request(`/api/planner/plans/${weekStart}`, { method: 'DELETE' })
}

export function reassign(weekStart, assignmentId, userId) {
  return request(`/api/planner/plans/${weekStart}/assignments/${assignmentId}`, {
    method: 'PUT',
    body: JSON.stringify({ userId }),
  })
}

// Domownicy, których zna planner (dostał ich przez Kafkę). Z nich wybieramy, kto ma zrobić zadanie.
export function getPlannerMembers() {
  return request('/api/planner/households/me/members')
}

// Domownicy z user-auth-service: tu są e-maile i informacja, kto może się logować.
export function getMembers() {
  return request('/api/households/me/members')
}

export function addMember(member) {
  return request('/api/households/me/members', {
    method: 'POST',
    body: JSON.stringify(member),
  })
}

export function deleteMember(userId) {
  return request(`/api/households/me/members/${userId}`, { method: 'DELETE' })
}

// ----- Obowiązki (Krok 13a3) -----

export function getChores() {
  return request('/api/planner/chores')
}

export function addChore(chore) {
  return request('/api/planner/chores', { method: 'POST', body: JSON.stringify(chore) })
}

// PUT podmienia cały obowiązek, więc wysyłamy wszystkie pola, także te bez zmian.
export function updateChore(choreId, chore) {
  return request(`/api/planner/chores/${choreId}`, { method: 'PUT', body: JSON.stringify(chore) })
}

export function deleteChore(choreId) {
  return request(`/api/planner/chores/${choreId}`, { method: 'DELETE' })
}

// Lista userId osób, które NIE robią tego obowiązku.
export function getExclusions(choreId) {
  return request(`/api/planner/chores/${choreId}/exclusions`)
}

export function excludeMember(choreId, userId) {
  return request(`/api/planner/chores/${choreId}/exclusions/${userId}`, { method: 'PUT' })
}

export function includeMember(choreId, userId) {
  return request(`/api/planner/chores/${choreId}/exclusions/${userId}`, { method: 'DELETE' })
}

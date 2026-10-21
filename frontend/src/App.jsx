import { useCallback, useState } from 'react'
import { clearToken, getToken } from './api.js'
import CalendarPage from './CalendarPage.jsx'
import LoginPage from './LoginPage.jsx'

// Główny komponent: jest token = kalendarz, nie ma tokenu = logowanie.
export default function App() {
  const [loggedIn, setLoggedIn] = useState(() => getToken() !== null)
  const [message, setMessage] = useState(null)

  const logout = useCallback(() => {
    clearToken()
    setMessage(null)
    setLoggedIn(false)
  }, [])

  // Wołane, gdy backend odpowie 401 (np. token wygasł po godzinie).
  const handleUnauthorized = useCallback((error) => {
    clearToken()
    setMessage(error?.message ?? 'Zaloguj się ponownie.')
    setLoggedIn(false)
  }, [])

  if (!loggedIn) {
    return <LoginPage message={message} onLoggedIn={() => setLoggedIn(true)} />
  }
  return <CalendarPage onLogout={logout} onUnauthorized={handleUnauthorized} />
}

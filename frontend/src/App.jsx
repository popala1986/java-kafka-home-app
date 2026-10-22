import { useCallback, useEffect, useState } from 'react'
import { clearToken, getMe, getToken, UnauthorizedError } from './api.js'
import CalendarPage from './CalendarPage.jsx'
import ChoresPage from './ChoresPage.jsx'
import FamilyPlanPage from './FamilyPlanPage.jsx'
import LoginPage from './LoginPage.jsx'
import MembersPage from './MembersPage.jsx'
import NotificationsPage from './NotificationsPage.jsx'

// Zakładki u góry ekranu. adminOnly = widzi je tylko rodzic.
const TABS = [
  { id: 'calendar', label: 'Mój tydzień', adminOnly: false },
  { id: 'family', label: 'Plan rodziny', adminOnly: true },
  { id: 'chores', label: 'Obowiązki', adminOnly: true },
  { id: 'members', label: 'Domownicy', adminOnly: true },
  { id: 'notifications', label: 'Powiadomienia', adminOnly: false },
]

// Główny komponent: nie ma tokenu = logowanie, jest token = górny pasek, zakładki i wybrana strona.
export default function App() {
  const [loggedIn, setLoggedIn] = useState(() => getToken() !== null)
  const [message, setMessage] = useState(null)
  const [me, setMe] = useState(null)
  const [tab, setTab] = useState('calendar')

  const logout = useCallback(() => {
    clearToken()
    setMessage(null)
    setMe(null)
    setTab('calendar')
    setLoggedIn(false)
  }, [])

  // Wołane, gdy backend odpowie 401 (np. token wygasł po godzinie).
  const handleUnauthorized = useCallback((error) => {
    clearToken()
    setMessage(error?.message ?? 'Zaloguj się ponownie.')
    setMe(null)
    setTab('calendar')
    setLoggedIn(false)
  }, [])

  // Po zalogowaniu pobieramy "kim jestem": imię do powitania i rolę (USER albo ADMIN).
  useEffect(() => {
    if (!loggedIn) {
      return
    }
    getMe()
      .then(setMe)
      .catch((e) => (e instanceof UnauthorizedError ? handleUnauthorized(e) : setMessage(e.message)))
  }, [loggedIn, handleUnauthorized])

  if (!loggedIn) {
    return <LoginPage message={message} onLoggedIn={() => setLoggedIn(true)} />
  }

  // Ukrycie zakładek to tylko wygoda. Prawdziwą ochroną jest hasRole("ADMIN") w backendzie.
  const isAdmin = me?.role === 'ADMIN'
  const visibleTabs = TABS.filter((t) => isAdmin || !t.adminOnly)

  return (
    <main className="page">
      <header className="topbar">
        <span>Cześć, {me ? me.firstName : '…'}!</span>
        <button className="link" onClick={logout}>Wyloguj</button>
      </header>

      {visibleTabs.length > 1 && (
        <nav className="tabs">
          {visibleTabs.map((t) => (
            <button key={t.id} className={t.id === tab ? 'active' : ''} onClick={() => setTab(t.id)}>
              {t.label}
            </button>
          ))}
        </nav>
      )}

      {message && <p className="error">{message}</p>}

      {tab === 'calendar' && <CalendarPage onUnauthorized={handleUnauthorized} />}
      {isAdmin && tab === 'family' && <FamilyPlanPage onUnauthorized={handleUnauthorized} />}
      {isAdmin && tab === 'chores' && <ChoresPage onUnauthorized={handleUnauthorized} />}
      {isAdmin && tab === 'members' && <MembersPage onUnauthorized={handleUnauthorized} />}
      {tab === 'notifications' && <NotificationsPage onUnauthorized={handleUnauthorized} />}
    </main>
  )
}

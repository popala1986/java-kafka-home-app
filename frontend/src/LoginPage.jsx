import { useState } from 'react'
import { login } from './api.js'

// Formularz logowania. Po udanym logowaniu woła onLoggedIn(), a App pokazuje kalendarz.
export default function LoginPage({ onLoggedIn, message }) {
  // useState = "zmienna, której zmiana przerysowuje ekran". [wartość, funkcja do zmiany]
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(message)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault() // bez tego przeglądarka przeładowałaby całą stronę
    setError(null)
    setLoading(true)
    try {
      await login(email, password)
      onLoggedIn()
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="page">
      <h1>Domowe obowiązki</h1>
      <form className="card" onSubmit={handleSubmit}>
        <label>
          E-mail
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)}
                 autoComplete="username" required />
        </label>
        <label>
          Hasło
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)}
                 autoComplete="current-password" required />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? 'Loguję…' : 'Zaloguj'}
        </button>
      </form>
    </main>
  )
}

import { useCallback, useEffect, useState } from 'react'
import { addMember, deleteMember, getMembers, UnauthorizedError } from './api.js'
import { formatIsoDate } from './dates.js'

const EMPTY_FORM = { firstName: '', birthDate: '', canLogIn: false, email: '', password: '' }

// "Domownicy" (tylko rodzic): lista rodziny i formularz dodania nowej osoby.
export default function MembersPage({ onUnauthorized }) {
  const [members, setMembers] = useState([])
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)

  const handleError = useCallback(
    (e) => (e instanceof UnauthorizedError ? onUnauthorized(e) : setError(e.message)),
    [onUnauthorized],
  )

  const loadMembers = useCallback(() => {
    getMembers().then(setMembers).catch(handleError)
  }, [handleError])

  useEffect(() => {
    loadMembers()
  }, [loadMembers])

  // Jedna funkcja dla wszystkich pól: zmienia tylko to pole, które podasz po nazwie.
  function updateField(name, value) {
    setForm((current) => ({ ...current, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    setSuccess(null)
    try {
      // Bez konta = email i hasło puste (null). Tak samo jak w Postmanie w Kroku 6.
      const added = await addMember({
        firstName: form.firstName.trim(),
        birthDate: form.birthDate,
        email: form.canLogIn ? form.email : null,
        password: form.canLogIn ? form.password : null,
      })
      setSuccess(`Dodano: ${added.firstName}. Planner dostanie tę osobę przez Kafkę w ciągu kilku sekund.`)
      setForm(EMPTY_FORM)
      loadMembers()
    } catch (e) {
      handleError(e)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(member) {
    if (!window.confirm(`Usunąć domownika ${member.firstName}? Zadania tej osoby znikną z planów.`)) {
      return
    }
    setError(null)
    setSuccess(null)
    try {
      await deleteMember(member.id)
      setSuccess(`Usunięto: ${member.firstName}. Planner i powiadomienia dostaną to przez Kafkę.`)
      loadMembers()
    } catch (e) {
      handleError(e)
    }
  }

  return (
    <>
      {/* Komunikaty na górze, żeby było je widać i po dodaniu, i po usunięciu. */}
      {error && <p className="error">{error}</p>}
      {success && <p className="success">{success}</p>}

      <section className="card">
        <h2>Rodzina</h2>
        <ul>
          {members.map((member) => (
            <li key={member.id}>
              <span>
                {member.firstName}
                <span className="points"> · ur. {formatIsoDate(member.birthDate)}</span>
              </span>
              <span className="points">
                {member.role === 'ADMIN' ? 'rodzic' : member.canLogIn ? 'ma konto' : 'bez konta'}
                {/* Rodzica usunąć się nie da (backend odpowie 409), więc przycisku przy nim nie ma. */}
                {member.role !== 'ADMIN' && (
                  <button className="link danger-link" onClick={() => handleDelete(member)}
                          aria-label={`Usuń ${member.firstName}`}>
                    Usuń
                  </button>
                )}
              </span>
            </li>
          ))}
        </ul>
      </section>

      <form className="card" onSubmit={handleSubmit}>
        <h2>Dodaj domownika</h2>
        <label>
          Imię
          <input value={form.firstName} onChange={(e) => updateField('firstName', e.target.value)} required />
        </label>
        <label>
          Data urodzenia
          <input type="date" value={form.birthDate}
                 onChange={(e) => updateField('birthDate', e.target.value)} required />
        </label>
        <label className="checkbox">
          <input type="checkbox" checked={form.canLogIn}
                 onChange={(e) => updateField('canLogIn', e.target.checked)} />
          Może się logować (ma własny telefon)
        </label>
        {/* Pola e-mail i hasło pokazują się tylko, gdy zaznaczysz "Może się logować". */}
        {form.canLogIn && (
          <>
            <label>
              E-mail
              <input type="email" value={form.email}
                     onChange={(e) => updateField('email', e.target.value)} required />
            </label>
            <label>
              Hasło (min. 8 znaków)
              <input type="password" value={form.password} autoComplete="new-password"
                     onChange={(e) => updateField('password', e.target.value)} required minLength={8} />
            </label>
          </>
        )}
        <button type="submit" disabled={saving}>{saving ? 'Zapisuję…' : 'Dodaj'}</button>
      </form>
    </>
  )
}

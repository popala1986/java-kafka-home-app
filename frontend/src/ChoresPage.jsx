import { useCallback, useEffect, useState } from 'react'
import {
  addChore, deleteChore, excludeMember, getChores, getExclusions, getPlannerMembers, includeMember,
  UnauthorizedError, updateChore,
} from './api.js'
import ChoreForm, { FREQUENCY_LABELS, TYPE_LABELS } from './ChoreForm.jsx'

// Z odpowiedzi backendu (ChoreResponse) robimy body do PUT (ChoreRequest): bez id i code.
function toRequest(chore) {
  const { name, type, frequency, minAge, durationMinutes, points, active } = chore
  return { name, type, frequency, minAge, durationMinutes, points, active }
}

// "Obowiązki" (tylko rodzic): które obowiązki ma Twoja rodzina, z jakimi punktami i kto ich nie robi.
export default function ChoresPage({ onUnauthorized }) {
  const [chores, setChores] = useState([])
  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)
  const [adding, setAdding] = useState(false)
  // Który obowiązek jest rozwinięty do edycji (jego id) i kto go nie robi.
  const [editingId, setEditingId] = useState(null)
  const [excluded, setExcluded] = useState([])

  const handleError = useCallback(
    (e) => (e instanceof UnauthorizedError ? onUnauthorized(e) : setError(e.message)),
    [onUnauthorized],
  )

  useEffect(() => {
    Promise.all([getChores(), getPlannerMembers()])
      .then(([loadedChores, loadedMembers]) => {
        setChores(loadedChores)
        setMembers(loadedMembers)
      })
      .catch(handleError)
      .finally(() => setLoading(false))
  }, [handleError])

  // Podmienia jeden obowiązek w liście po odpowiedzi z backendu.
  function replaceChore(updated) {
    setChores((list) => list.map((c) => (c.id === updated.id ? updated : c)))
  }

  function clearMessages() {
    setError(null)
    setSuccess(null)
  }

  async function handleToggle(chore) {
    clearMessages()
    try {
      replaceChore(await updateChore(chore.id, { ...toRequest(chore), active: !chore.active }))
    } catch (e) {
      handleError(e)
    }
  }

  async function handleAdd(request) {
    clearMessages()
    try {
      const added = await addChore(request)
      setChores((list) => [...list, added])
      setAdding(false)
      setSuccess(`Dodano: ${added.name}.`)
    } catch (e) {
      handleError(e)
    }
  }

  async function handleUpdate(chore, request) {
    clearMessages()
    try {
      replaceChore(await updateChore(chore.id, request))
      setEditingId(null)
      setSuccess(`Zapisano: ${request.name}.`)
    } catch (e) {
      handleError(e)
    }
  }

  async function handleDelete(chore) {
    if (!window.confirm(`Usunąć obowiązek „${chore.name}”? Zniknie też z ułożonych planów.`)) {
      return
    }
    clearMessages()
    try {
      await deleteChore(chore.id)
      setChores((list) => list.filter((c) => c.id !== chore.id))
      setEditingId(null)
      setSuccess(`Usunięto: ${chore.name}.`)
    } catch (e) {
      handleError(e)
    }
  }

  async function handleEdit(chore) {
    clearMessages()
    if (editingId === chore.id) {
      setEditingId(null)
      return
    }
    try {
      setExcluded(await getExclusions(chore.id))
      setEditingId(chore.id)
    } catch (e) {
      handleError(e)
    }
  }

  // Zaznaczone pole = ta osoba robi obowiązek. Odznaczenie dodaje wykluczenie.
  // Pole zmieniamy od razu, a gdy backend odmówi, wracamy do poprzedniego stanu.
  async function handleMemberToggle(chore, userId, doesIt) {
    clearMessages()
    const previous = excluded
    setExcluded(doesIt ? excluded.filter((id) => id !== userId) : [...excluded, userId])
    try {
      if (doesIt) {
        await includeMember(chore.id, userId)
      } else {
        await excludeMember(chore.id, userId)
      }
    } catch (e) {
      setExcluded(previous)
      handleError(e)
    }
  }

  const activeCount = chores.filter((c) => c.active).length

  return (
    <>
      {error && <p className="error">{error}</p>}
      {success && <p className="success">{success}</p>}

      <p className="muted">
        Włączonych: {activeCount} z {chores.length}. Zmiany działają w kolejnych planach. Żeby objęły
        ułożony już tydzień, w „Plan rodziny” usuń plan i ułóż go od nowa.
      </p>

      {adding ? (
        <section className="card">
          <h2>Nowy obowiązek</h2>
          <ChoreForm submitLabel="Dodaj" onSubmit={handleAdd} onCancel={() => setAdding(false)} />
        </section>
      ) : (
        <button className="full" onClick={() => setAdding(true)}>+ Dodaj obowiązek</button>
      )}

      {loading && <p className="muted">Ładuję…</p>}

      <section className="card">
        <ul>
          {chores.map((chore) => (
            <li key={chore.id} className={chore.active ? 'chore' : 'chore inactive'}>
              <div className="chore-head">
                <label className="checkbox">
                  <input type="checkbox" checked={chore.active} onChange={() => handleToggle(chore)}
                         aria-label={`Włączony: ${chore.name}`} />
                  <span>
                    {chore.name}
                    <span className="chore-meta">
                      {chore.points} pkt · {FREQUENCY_LABELS[chore.frequency]} · od {chore.minAge} lat
                      {' '}· {TYPE_LABELS[chore.type]}
                    </span>
                  </span>
                </label>
                <button className="link" onClick={() => handleEdit(chore)}>
                  {editingId === chore.id ? 'Zwiń' : 'Edytuj'}
                </button>
              </div>

              {editingId === chore.id && (
                <div className="chore-edit">
                  <ChoreForm initial={chore} submitLabel="Zapisz"
                             onSubmit={(request) => handleUpdate(chore, request)}
                             onCancel={() => setEditingId(null)} />
                  <h2>Kto robi</h2>
                  {members.map((member) => (
                    <label key={member.userId} className="checkbox">
                      <input type="checkbox" checked={!excluded.includes(member.userId)}
                             onChange={(e) => handleMemberToggle(chore, member.userId, e.target.checked)} />
                      {member.firstName} ({member.age} l.)
                    </label>
                  ))}
                  <p className="muted">„Kto robi” zapisuje się od razu, bez „Zapisz”. Osoby młodsze niż „od ilu lat” i tak nie dostaną tego obowiązku.</p>
                  <button className="danger" onClick={() => handleDelete(chore)}>Usuń obowiązek</button>
                </div>
              )}
            </li>
          ))}
        </ul>
      </section>
    </>
  )
}

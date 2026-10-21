import { useCallback, useEffect, useState } from 'react'
import {
  deletePlan, generatePlan, getFamilyPlan, getPlannerMembers, reassign, UnauthorizedError,
} from './api.js'
import { addDays, dayLabel, mondayOf, toIsoDate } from './dates.js'
import WeekNav from './WeekNav.jsx'

// "Plan rodziny" (tylko rodzic): ułóż plan na tydzień, zmień, kto co robi, albo usuń plan.
export default function FamilyPlanPage({ onUnauthorized }) {
  const [weekStart, setWeekStart] = useState(() => mondayOf(new Date()))
  const [assignments, setAssignments] = useState([])
  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  const week = toIsoDate(weekStart)

  const handleError = useCallback(
    (e) => (e instanceof UnauthorizedError ? onUnauthorized(e) : setError(e.message)),
    [onUnauthorized],
  )

  // Lista domowników do wyboru w polu "kto robi". Pobieramy raz.
  useEffect(() => {
    getPlannerMembers().then(setMembers).catch(handleError)
  }, [handleError])

  useEffect(() => {
    setLoading(true)
    setError(null)
    getFamilyPlan(week)
      .then(setAssignments)
      .catch(handleError)
      .finally(() => setLoading(false))
  }, [week, handleError])

  async function handleGenerate() {
    setBusy(true)
    setError(null)
    try {
      setAssignments(await generatePlan(week))
    } catch (e) {
      handleError(e)
    } finally {
      setBusy(false)
    }
  }

  async function handleDelete() {
    // window.confirm = okienko "OK / Anuluj" przeglądarki. Usunięcia nie da się cofnąć.
    if (!window.confirm(`Usunąć plan na tydzień od ${dayLabel(weekStart)}?`)) {
      return
    }
    setBusy(true)
    setError(null)
    try {
      await deletePlan(week)
      setAssignments([])
    } catch (e) {
      handleError(e)
    } finally {
      setBusy(false)
    }
  }

  async function handleReassign(assignmentId, userId) {
    setError(null)
    try {
      const updated = await reassign(week, assignmentId, userId)
      // Podmieniamy w liście tylko to jedno zadanie, reszta zostaje bez zmian.
      setAssignments((list) => list.map((a) => (a.id === updated.id ? updated : a)))
    } catch (e) {
      handleError(e)
    }
  }

  const days = [0, 1, 2, 3, 4, 5, 6].map((offset) => addDays(weekStart, offset))
  const pointsByMember = members.map((member) => ({
    ...member,
    points: assignments
      .filter((a) => a.userId === member.userId)
      .reduce((sum, a) => sum + a.points, 0),
  }))

  return (
    <>
      <WeekNav weekStart={weekStart} onChange={setWeekStart} />

      {loading && <p className="muted">Ładuję…</p>}
      {error && <p className="error">{error}</p>}

      {!loading && assignments.length === 0 && (
        <section className="card">
          <p className="muted">Na ten tydzień nie ma jeszcze planu.</p>
          <button onClick={handleGenerate} disabled={busy}>
            {busy ? 'Układam…' : 'Ułóż plan'}
          </button>
        </section>
      )}

      {!loading && assignments.length > 0 && (
        <>
          <section className="card">
            <h2>Punkty w tym tygodniu</h2>
            <ul>
              {pointsByMember.map((member) => (
                <li key={member.userId}>
                  <span>{member.firstName}</span>
                  <span className="points">{member.points} pkt</span>
                </li>
              ))}
            </ul>
          </section>

          {days.map((day) => {
            const iso = toIsoDate(day)
            const tasks = assignments.filter((a) => a.date === iso)
            return (
              <section key={iso} className="card">
                <h2>{dayLabel(day)}</h2>
                <ul>
                  {tasks.map((task) => (
                    <li key={task.id}>
                      <span>
                        {task.choreName}
                        <span className="points"> · {task.points} pkt{task.manual && ' · zmienione'}</span>
                      </span>
                      <select value={task.userId}
                              onChange={(e) => handleReassign(task.id, e.target.value)}
                              aria-label={`Kto robi: ${task.choreName}`}>
                        {members.map((member) => (
                          <option key={member.userId} value={member.userId}>{member.firstName}</option>
                        ))}
                      </select>
                    </li>
                  ))}
                </ul>
              </section>
            )
          })}

          <button className="danger" onClick={handleDelete} disabled={busy}>Usuń plan tygodnia</button>
        </>
      )}
    </>
  )
}

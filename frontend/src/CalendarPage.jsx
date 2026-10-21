import { useCallback, useEffect, useState } from 'react'
import { getMyPlan, UnauthorizedError } from './api.js'
import { addDays, dayLabel, mondayOf, toIsoDate } from './dates.js'
import WeekNav from './WeekNav.jsx'

// "Mój tydzień": zadania zalogowanej osoby na wybrany tydzień, dzień po dniu.
export default function CalendarPage({ onUnauthorized }) {
  const [weekStart, setWeekStart] = useState(() => mondayOf(new Date()))
  const [assignments, setAssignments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // 401 = wyloguj (App pokaże logowanie), każdy inny błąd = pokaż komunikat.
  const handleError = useCallback(
    (e) => (e instanceof UnauthorizedError ? onUnauthorized(e) : setError(e.message)),
    [onUnauthorized],
  )

  // [weekStart] = za każdym razem, gdy zmieni się tydzień (strzałki), pobierz plan od nowa.
  useEffect(() => {
    setLoading(true)
    setError(null)
    getMyPlan(toIsoDate(weekStart))
      .then(setAssignments)
      .catch(handleError)
      .finally(() => setLoading(false))
  }, [weekStart, handleError])

  const today = toIsoDate(new Date())
  const days = [0, 1, 2, 3, 4, 5, 6].map((offset) => addDays(weekStart, offset))
  const totalPoints = assignments.reduce((sum, a) => sum + a.points, 0)

  return (
    <>
      <WeekNav weekStart={weekStart} onChange={setWeekStart} />

      {loading && <p className="muted">Ładuję…</p>}
      {error && <p className="error">{error}</p>}
      {!loading && !error && assignments.length === 0 && (
        <p className="muted">Na ten tydzień nie masz zadań (albo plan nie jest jeszcze ułożony).</p>
      )}

      {!loading && assignments.length > 0 && (
        <>
          <p className="muted">Razem w tym tygodniu: {totalPoints} pkt</p>
          {days.map((day) => {
            const iso = toIsoDate(day)
            const tasks = assignments.filter((a) => a.date === iso)
            return (
              <section key={iso} className={iso === today ? 'card today' : 'card'}>
                <h2>{dayLabel(day)}{iso === today && ' · dziś'}</h2>
                {tasks.length === 0 ? (
                  <p className="muted">Wolne 🎉</p>
                ) : (
                  <ul>
                    {tasks.map((task) => (
                      <li key={task.id}>
                        <span>{task.choreName}</span>
                        <span className="points">{task.points} pkt</span>
                      </li>
                    ))}
                  </ul>
                )}
              </section>
            )
          })}
        </>
      )}
    </>
  )
}

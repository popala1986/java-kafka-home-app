import { useState } from 'react'

// Polskie nazwy dla wartości enumów z backendu (ChoreType, Frequency).
export const TYPE_LABELS = {
  PERSONAL: 'każdy dla siebie',
  SHARED: 'jeden dla całego domu',
}

export const FREQUENCY_LABELS = {
  DAILY: 'codziennie',
  THREE_TIMES_A_WEEK: '3 razy w tygodniu',
  WEEKLY: 'raz w tygodniu',
  MONTHLY: 'raz w miesiącu',
}

const EMPTY_CHORE = {
  name: '', type: 'SHARED', frequency: 'WEEKLY', minAge: 6, durationMinutes: 10, points: 1, active: true,
}

// Formularz obowiązku: ten sam do dodawania i do edycji (jak ChoreRequest w backendzie).
// initial = obowiązek do edycji albo nic (wtedy pusty formularz).
export default function ChoreForm({ initial, submitLabel, onSubmit, onCancel }) {
  const [form, setForm] = useState(() => ({ ...EMPTY_CHORE, ...initial }))
  const [saving, setSaving] = useState(false)

  function updateField(name, value) {
    setForm((current) => ({ ...current, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSaving(true)
    try {
      // Pola z <input type="number"> przychodzą jako tekst ("6"), a backend chce liczby.
      await onSubmit({
        name: form.name.trim(),
        type: form.type,
        frequency: form.frequency,
        minAge: Number(form.minAge),
        durationMinutes: Number(form.durationMinutes),
        points: Number(form.points),
        active: form.active,
      })
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="chore-form" onSubmit={handleSubmit}>
      <label>
        Nazwa
        <input value={form.name} onChange={(e) => updateField('name', e.target.value)} required maxLength={200} />
      </label>
      <label>
        Rodzaj
        <select value={form.type} onChange={(e) => updateField('type', e.target.value)}>
          {Object.entries(TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
      </label>
      <label>
        Jak często
        <select value={form.frequency} onChange={(e) => updateField('frequency', e.target.value)}>
          {Object.entries(FREQUENCY_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
      </label>
      <div className="row">
        <label>
          Od ilu lat
          <input type="number" min={0} max={99} value={form.minAge}
                 onChange={(e) => updateField('minAge', e.target.value)} required />
        </label>
        <label>
          Minut
          <input type="number" min={1} max={600} value={form.durationMinutes}
                 onChange={(e) => updateField('durationMinutes', e.target.value)} required />
        </label>
        <label>
          Punkty
          <select value={form.points} onChange={(e) => updateField('points', e.target.value)}>
            <option value={1}>1</option>
            <option value={2}>2</option>
            <option value={3}>3</option>
          </select>
        </label>
      </div>
      <label className="checkbox">
        <input type="checkbox" checked={form.active} onChange={(e) => updateField('active', e.target.checked)} />
        Włączony (trafia do planu)
      </label>
      <div className="actions">
        <button type="submit" disabled={saving}>{saving ? 'Zapisuję…' : submitLabel}</button>
        <button type="button" className="secondary" onClick={onCancel}>Anuluj</button>
      </div>
    </form>
  )
}

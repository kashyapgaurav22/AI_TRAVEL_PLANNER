import { useState } from 'react';

const presetInterests = ['adventure', 'nature', 'food', 'culture'];
const destinationOptions = ['Jaipur', 'Bali', 'Tokyo', 'Barcelona', 'Cape Town'];

export default function PlannerForm({ onSubmit, loading, initialData }) {
  const [form, setForm] = useState(
    initialData || {
      destination: '',
      budget: 1200,
      duration: 5,
      interests: ['nature', 'food'],
    },
  );

  const toggleInterest = (interest) => {
    setForm((prev) => {
      const exists = prev.interests.includes(interest);
      return {
        ...prev,
        interests: exists
          ? prev.interests.filter((item) => item !== interest)
          : [...prev.interests, interest],
      };
    });
  };

  return (
    <form
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit({
          ...form,
          budget: Number(form.budget),
          duration: Number(form.duration),
        });
      }}
      className="rise card-frost rounded-3xl border border-white/40 p-6"
    >
      <h3 className="font-heading text-2xl font-bold text-ink">Plan a Trip</h3>
      <div className="mt-5 grid gap-4 md:grid-cols-2">
        <div className="space-y-1">
          <label htmlFor="destination" className="text-sm font-semibold text-ink/80">
            Destination
          </label>
          <input
            id="destination"
            list="destinations"
            className="w-full rounded-xl border border-ink/20 bg-white/90 px-4 py-3 outline-none focus:border-mint"
            placeholder="Type destination"
            value={form.destination}
            onChange={(event) => setForm((prev) => ({ ...prev, destination: event.target.value }))}
          />
          <datalist id="destinations">
            {destinationOptions.map((item) => (
              <option key={item} value={item} />
            ))}
          </datalist>
        </div>
        <div className="space-y-1">
          <label htmlFor="money" className="text-sm font-semibold text-ink/80">
            Money (Budget)
          </label>
          <input
            id="money"
            className="w-full rounded-xl border border-ink/20 bg-white/90 px-4 py-3 outline-none focus:border-mint"
            type="number"
            min="1"
            placeholder="Budget"
            value={form.budget}
            onChange={(event) => setForm((prev) => ({ ...prev, budget: event.target.value }))}
            required
          />
        </div>
        <div className="space-y-1 md:col-span-2">
          <label htmlFor="days" className="text-sm font-semibold text-ink/80">
            Days
          </label>
          <input
            id="days"
            className="w-full rounded-xl border border-ink/20 bg-white/90 px-4 py-3 outline-none focus:border-mint"
            type="number"
            min="1"
            max="30"
            placeholder="Duration (days)"
            value={form.duration}
            onChange={(event) => setForm((prev) => ({ ...prev, duration: event.target.value }))}
            required
          />
        </div>
      </div>

      <div className="mt-5 flex flex-wrap gap-2">
        {presetInterests.map((interest) => {
          const active = form.interests.includes(interest);
          return (
            <button
              key={interest}
              type="button"
              onClick={() => toggleInterest(interest)}
              className={`rounded-full px-4 py-2 text-sm font-semibold transition ${
                active ? 'bg-ember text-white' : 'bg-white text-ink'
              }`}
            >
              {interest}
            </button>
          );
        })}
      </div>

      <button
        type="submit"
        disabled={loading || form.interests.length === 0}
        className="mt-6 rounded-xl bg-mint px-5 py-3 font-semibold text-white transition hover:brightness-110 disabled:opacity-60"
      >
        {loading ? 'Generating itinerary...' : 'Generate AI Itinerary'}
      </button>
    </form>
  );
}

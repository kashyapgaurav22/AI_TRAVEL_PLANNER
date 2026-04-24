import ItineraryView from '../components/ItineraryView';

export default function ItineraryPage({ user, plan, onBack }) {
  return (
    <div className="mx-auto max-w-7xl px-4 py-8 md:px-8">
      <header className="rise mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.2em] text-ink/70">AI Travel Planner</p>
          <h1 className="font-heading text-4xl font-extrabold text-ink">Itinerary for {user.name}</h1>
        </div>
        <button
          type="button"
          onClick={onBack}
          className="rounded-xl bg-ink px-4 py-2 text-sm font-semibold text-white"
        >
          Back to Planner
        </button>
      </header>

      <ItineraryView plan={plan} />
    </div>
  );
}

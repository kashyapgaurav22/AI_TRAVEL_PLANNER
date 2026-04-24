import PlannerForm from '../components/PlannerForm';
import TripHistory from '../components/TripHistory';

export default function DashboardPage({
  user,
  onLogout,
  onPlan,
  onUpdate,
  loading,
  currentPlan,
  trips,
  error,
  onViewLatestPlan,
}) {
  return (
    <div className="mx-auto max-w-7xl px-4 py-8 md:px-8">
      <header className="rise mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.2em] text-ink/70">AI Travel Planner</p>
          <h1 className="font-heading text-4xl font-extrabold text-ink">Hello, {user.name}</h1>
        </div>
        <button
          type="button"
          onClick={onLogout}
          className="rounded-xl bg-ember px-4 py-2 text-sm font-semibold text-white"
        >
          Logout
        </button>
      </header>

      {error && <p className="mb-4 rounded-xl bg-red-100 px-4 py-3 text-sm font-semibold text-red-700">{error}</p>}

      {currentPlan && (
        <div className="mb-4">
          <button
            type="button"
            onClick={onViewLatestPlan}
            className="rounded-xl bg-ink px-4 py-2 text-sm font-semibold text-white"
          >
            View Latest Itinerary
          </button>
        </div>
      )}

      <div className="space-y-6">
        <PlannerForm loading={loading} onSubmit={onPlan} />
        <TripHistory trips={trips} onReuseTrip={onUpdate} />
      </div>
    </div>
  );
}

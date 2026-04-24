export default function TripHistory({ trips, onReuseTrip }) {
  return (
    <div className="card-frost rounded-3xl border border-white/40 p-6">
      <h3 className="font-heading text-2xl font-bold text-ink">Trip History</h3>
      {trips.length === 0 ? (
        <p className="mt-3 text-sm text-ink/80">No trips yet. Your generated plans will appear here.</p>
      ) : (
        <div className="mt-4 space-y-3">
          {trips.map((trip) => (
            <div key={trip.tripId} className="rounded-xl bg-white/80 p-4">
              <p className="font-semibold text-ink">{trip.destination}</p>
              <p className="text-sm text-ink/80">
                ${trip.budget} • {trip.duration} days • {new Date(trip.createdAt).toLocaleDateString()}
              </p>
              <div className="mt-2 flex gap-2">
                {trip.interests.map((interest) => (
                  <span key={`${trip.tripId}-${interest}`} className="rounded-full bg-sand px-2 py-1 text-xs font-medium">
                    {interest}
                  </span>
                ))}
              </div>
              <button
                type="button"
                onClick={() => onReuseTrip(trip)}
                className="mt-3 rounded-lg bg-ink px-3 py-2 text-xs font-semibold text-white"
              >
                Regenerate Plan
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

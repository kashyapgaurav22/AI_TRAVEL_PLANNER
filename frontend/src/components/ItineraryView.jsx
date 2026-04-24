export default function ItineraryView({ plan }) {
  if (!plan) {
    return (
      <div className="card-frost rounded-3xl border border-white/40 p-6">
        <h3 className="font-heading text-2xl font-bold text-ink">Itinerary Output</h3>
        <p className="mt-3 text-ink/80">Generate a trip to view destination recommendations, daily plan, hotels, and attractions.</p>
      </div>
    );
  }

  return (
    <div className="rise space-y-5">
      <div className="card-frost rounded-3xl border border-white/40 p-6">
        <p className="text-xs font-semibold uppercase tracking-wide text-ink/70">Suggested Destination</p>
        <h3 className="font-heading mt-1 text-3xl font-extrabold text-ink">{plan.destination}</h3>
        <p className="mt-2 text-ink/80">Estimated Cost: ${plan.estimatedCost}</p>
      </div>

      <div className="card-frost rounded-3xl border border-white/40 p-6">
        <h4 className="font-heading text-xl font-bold text-ink">Day-wise Itinerary</h4>
        <ul className="mt-3 space-y-2">
          {plan.itinerary.map((item, idx) => (
            <li key={idx} className="rounded-lg bg-white/80 px-3 py-2 text-sm text-ink">
              {item}
            </li>
          ))}
        </ul>
      </div>

      <div className="grid gap-5 md:grid-cols-2">
        <div className="card-frost rounded-3xl border border-white/40 p-6">
          <h4 className="font-heading text-xl font-bold text-ink">Hotels</h4>
          <ul className="mt-3 space-y-2 text-sm">
            {plan.hotels.map((hotel) => (
              <li key={`${hotel.name}-${hotel.location}`} className="rounded-lg bg-white/80 px-3 py-2">
                <p className="font-semibold">{hotel.name}</p>
                <p>{hotel.location} • ${hotel.price}/night • {hotel.rating}★</p>
              </li>
            ))}
          </ul>
        </div>

        <div className="card-frost rounded-3xl border border-white/40 p-6">
          <h4 className="font-heading text-xl font-bold text-ink">Attractions</h4>
          <ul className="mt-3 space-y-2 text-sm">
            {plan.attractions.map((attraction) => (
              <li key={`${attraction.name}-${attraction.location}`} className="rounded-lg bg-white/80 px-3 py-2">
                <p className="font-semibold">{attraction.name}</p>
                <p>{attraction.category} • {attraction.location} • {attraction.rating}★</p>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

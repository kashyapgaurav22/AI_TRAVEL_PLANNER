import { useEffect, useState } from 'react';
import AuthPanel from './components/AuthPanel';
import DashboardPage from './pages/DashboardPage';
import ItineraryPage from './pages/ItineraryPage';
import { travelApi } from './services/api';

export default function App() {
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem('token');
    const name = localStorage.getItem('userName');
    return token ? { name: name || 'Traveler' } : null;
  });
  const [loading, setLoading] = useState(false);
  const [currentPlan, setCurrentPlan] = useState(null);
  const [trips, setTrips] = useState([]);
  const [error, setError] = useState('');
  const [activePage, setActivePage] = useState('dashboard');

  const loadHistory = async () => {
    try {
      const response = await travelApi.history();
      setTrips(response.data);
    } catch (err) {
      const message = err?.response?.data?.message || 'Failed to load trip history.';
      setError(message);
    }
  };

  useEffect(() => {
    if (user) {
      loadHistory();
    }
  }, [user]);

  const handlePlan = async (payload) => {
    setLoading(true);
    setError('');
    try {
      const response = await travelApi.plan(payload);
      setCurrentPlan(response.data);
      setActivePage('itinerary');
      await loadHistory();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to generate itinerary.');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async (trip) => {
    setLoading(true);
    setError('');
    try {
      const response = await travelApi.update({
        tripId: trip.tripId,
        destination: trip.destination,
        budget: trip.budget,
        duration: trip.duration,
        interests: trip.interests,
      });
      setCurrentPlan(response.data);
      setActivePage('itinerary');
      await loadHistory();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update itinerary.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    setUser(null);
    setTrips([]);
    setCurrentPlan(null);
    setError('');
    setActivePage('dashboard');
  };

  return (
    <main className="min-h-screen">
      {!user ? (
        <div className="mx-auto grid min-h-screen max-w-6xl items-center gap-8 px-4 py-10 md:grid-cols-2">
          <section className="rise space-y-4">
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-ink/70">AI Travel Planner</p>
            <h1 className="font-heading text-5xl font-extrabold leading-tight text-ink">
              Plan smarter journeys with a hybrid AI itinerary engine.
            </h1>
            <p className="max-w-md text-ink/80">
              Enter your budget, trip duration, and interests. Get destination scoring, day-wise activities,
              hotels, and attraction picks in one place.
            </p>
          </section>
          <AuthPanel onAuthenticated={setUser} />
        </div>
      ) : (
        <>
          {activePage === 'dashboard' && (
            <DashboardPage
              user={user}
              onLogout={handleLogout}
              onPlan={handlePlan}
              onUpdate={handleUpdate}
              loading={loading}
              currentPlan={currentPlan}
              trips={trips}
              error={error}
              onViewLatestPlan={() => setActivePage('itinerary')}
            />
          )}
          {activePage === 'itinerary' && (
            <ItineraryPage user={user} plan={currentPlan} onBack={() => setActivePage('dashboard')} />
          )}
        </>
      )}
    </main>
  );
}

import { useState } from 'react';
import { authApi } from '../services/api';

const initialState = { name: '', email: '', password: '' };

export default function AuthPanel({ onAuthenticated }) {
  const [isRegister, setIsRegister] = useState(false);
  const [form, setForm] = useState(initialState);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      const payload = isRegister
        ? { name: form.name, email: form.email, password: form.password }
        : { email: form.email, password: form.password };

      const response = isRegister
        ? await authApi.register(payload)
        : await authApi.login(payload);

      localStorage.setItem('token', response.data.token);
      localStorage.setItem('userName', response.data.name);
      onAuthenticated(response.data);
      setForm(initialState);
    } catch (err) {
      setError(err?.response?.data?.message || 'Authentication failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rise card-frost rounded-3xl border border-white/40 p-8 shadow-glow">
      <h2 className="font-heading text-3xl font-bold text-ink">
        {isRegister ? 'Create Account' : 'Welcome Back'}
      </h2>
      <p className="mt-2 text-sm text-ink/80">
        Build AI-assisted travel itineraries in seconds.
      </p>

      <form onSubmit={submit} className="mt-6 space-y-4">
        {isRegister && (
          <input
            className="w-full rounded-xl border border-ink/20 bg-white/90 px-4 py-3 outline-none transition focus:border-mint"
            type="text"
            name="name"
            placeholder="Your name"
            value={form.name}
            onChange={handleChange}
            required
          />
        )}
        <input
          className="w-full rounded-xl border border-ink/20 bg-white/90 px-4 py-3 outline-none transition focus:border-mint"
          type="email"
          name="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
          required
        />
        <input
          className="w-full rounded-xl border border-ink/20 bg-white/90 px-4 py-3 outline-none transition focus:border-mint"
          type="password"
          name="password"
          placeholder="Password"
          value={form.password}
          onChange={handleChange}
          required
          minLength={6}
        />

        {error && <p className="text-sm font-medium text-ember">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-xl bg-ink px-4 py-3 font-semibold text-sand transition hover:brightness-110 disabled:opacity-60"
        >
          {loading ? 'Please wait...' : isRegister ? 'Register' : 'Login'}
        </button>
      </form>

      <button
        type="button"
        onClick={() => setIsRegister((prev) => !prev)}
        className="mt-4 text-sm font-semibold text-ink/80 underline"
      >
        {isRegister ? 'Already have an account? Login' : "Need an account? Register"}
      </button>
    </div>
  );
}

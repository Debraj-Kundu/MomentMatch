'use client';

import { useState, type FormEvent } from 'react';
import { signIn } from 'next-auth/react';
import { ArrowUp, Sparkles } from 'lucide-react';

export function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    const result = await signIn('credentials', { email, password, redirect: false });

    setIsSubmitting(false);
    if (result?.error) setError('Email or password is incorrect.');
  }

  return (
    <main className="auth-shell">
      <div className="auth-art" aria-hidden="true">
        <div className="art-orbit orbit-one" />
        <div className="art-orbit orbit-two" />
        <div className="art-photo photo-one" />
        <div className="art-photo photo-two" />
        <div className="art-photo photo-three" />
        <div className="art-sticker">your memories,<br /><strong>beautifully found.</strong></div>
        <div className="art-caption"><span className="caption-dot" /> A calmer way to revisit everything.</div>
      </div>
      <section className="auth-panel">
        <div className="brand"><span className="brand-mark"><Sparkles size={17} /></span> reprise</div>
        <div className="auth-content">
          <p className="eyebrow">your visual memory</p>
          <h1>Welcome back.</h1>
          <p className="auth-subtitle">Pick up where you left off, or find a new moment in seconds.</p>
          <form onSubmit={handleSubmit} className="auth-form">
            <label><span>Email address</span><input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" autoComplete="email" /></label>
            <label><span>Password</span><input required minLength={5} type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="At least 5 characters" autoComplete="current-password" /></label>
            {error && <p className="auth-error" role="alert">{error}</p>}
            <button className="primary-button" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Signing in...' : 'Sign in'} <ArrowUp size={16} className="arrow-right" /></button>
          </form>
        </div>
        <p className="legal">By continuing, you agree to our <u>Terms</u> and <u>Privacy Policy</u>.</p>
      </section>
    </main>
  );
}

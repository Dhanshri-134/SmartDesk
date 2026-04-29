"use client";

import { useState } from "react";
import { createSupabaseBrowserClient } from "@/lib/supabase-browser";

export default function AdminLoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleLogin = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setStatus(null);

    try {
      const supabase = createSupabaseBrowserClient();
      const { data, error } = await supabase.auth.signInWithPassword({
        email: email.trim(),
        password,
      });

      if (error || !data.user) {
        setStatus(error?.message ?? "Unable to sign in.");
        return;
      }

      const { data: profile } = await supabase
        .from("profiles")
        .select("id, role, profile_completed, face_enrolled")
        .eq("id", data.user.id)
        .single();

      if (!profile || profile.role !== "company_admin") {
        await supabase.auth.signOut();
        setStatus("This account does not have company admin access.");
        return;
      }

      window.location.href = "/admin/dashboard";
    } catch (err) {
      setStatus("Unexpected error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-tr from-white-600 via-white-600 to-blue-500 px-4">
      
      <div className="w-full max-w-md backdrop-blur-lg bg-white/10 border border-white/20 rounded-2xl shadow-2xl p-8 text-white">
        
        {/* Header */}
        <div className="text-center mb-6">
          <p className="text-sm tracking-widest uppercase text-gray-200">
            SmartDesk Admin
          </p>
          <h2 className="text-indigo-600 text-2xl font-bold mt-2 ">Secure Company Login</h2>
          <p className="text-sm text-gray-500 mt-1">
            Use your admin credentials to continue
          </p>
        </div>

        {/* Alert */}
        {status && (
          <div className="mb-4 px-4 py-2 rounded-lg bg-red-500/20 border border-red-400 text-sm text-red-100">
            {status}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleLogin} className="space-y-4">
          
          <input
            type="email"
            placeholder="Work Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="w-full mb-2 px-3 py-2 rounded-lg bg-white/20 border border-white/30 placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-white text-white"
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="w-full mb-4 px-3 py-2 rounded-lg bg-white/20 border border-white/30 placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-white text-white"
          />

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 rounded-lg border-white/30 bg-white text-indigo-600 font-semibold hover:bg-gray-100 transition flex items-center justify-center"
          >
            {loading ? (
              <span className="animate-spin h-5 w-5 border-2 border-indigo-600 border-t-transparent rounded-full"></span>
            ) : (
              "Sign In"
            )}
          </button>
        </form>

        {/* Footer Links */}
        <div className="flex justify-between mt-6 text-sm text-gray-200">
          <a href="/admin/forgot" className="hover:underline">
            Forgot password?
          </a>
          <a href="/" className="hover:underline">
            Back to Home
          </a>
        </div>
      </div>
    </div>
  );
}

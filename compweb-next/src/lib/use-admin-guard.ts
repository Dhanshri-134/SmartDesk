"use client";

import { useEffect, useState } from "react";

import { createSupabaseBrowserClient } from "@/lib/supabase-browser";

type AdminProfile = {
  id: string;
  email: string;
  role: string;
  company_id: string | null;
  profile_completed: boolean;
  face_enrolled: boolean;
  face_hash?: string | null;
};

type GuardState = {
  loading: boolean;
  profile: AdminProfile | null;
};

export function useAdminGuard() {
  const [state, setState] = useState<GuardState>({ loading: true, profile: null });

  useEffect(() => {
    const supabase = createSupabaseBrowserClient();

    const run = async () => {
      const sessionResult = await supabase.auth.getSession();
      const session = sessionResult.data.session;
      if (!session) {
        window.location.href = "/admin/login";
        return;
      }

      const { data: profile } = await supabase
        .from("profiles")
        .select("id, email, role, company_id, profile_completed, face_enrolled, face_hash")
        .eq("id", session.user.id)
        .single();

      if (!profile || profile.role !== "company_admin") {
        await supabase.auth.signOut();
        window.location.href = "/admin/login";
        return;
      }

      if (!profile.profile_completed || !profile.face_enrolled) {
        // Face checks are disabled for the web app; allow access after credential login.
        // Keep onboarding data as-is without blocking access.
      }

      setState({ loading: false, profile });
    };

    run();
  }, []);

  return state;
}

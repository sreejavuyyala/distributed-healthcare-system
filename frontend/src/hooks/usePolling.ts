import { useEffect, useRef, useState } from 'react';

interface State<T> {
  data: T | null;
  error: string | null;
  loading: boolean;
}

/** Fetches `fn` immediately, then every `intervalMs` while `active` is true. */
export function usePolling<T>(fn: () => Promise<T>, intervalMs: number, active = true, deps: unknown[] = []) {
  const [state, setState] = useState<State<T>>({ data: null, error: null, loading: true });
  const fnRef = useRef(fn);
  fnRef.current = fn;

  useEffect(() => {
    let cancelled = false;

    async function run() {
      try {
        const data = await fnRef.current();
        if (!cancelled) setState({ data, error: null, loading: false });
      } catch (e) {
        if (!cancelled) setState((s) => ({ ...s, error: (e as Error).message, loading: false }));
      }
    }

    run();
    let id: number | undefined;
    if (active) {
      id = window.setInterval(run, intervalMs);
    }
    return () => {
      cancelled = true;
      if (id) window.clearInterval(id);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [active, intervalMs, ...deps]);

  return state;
}
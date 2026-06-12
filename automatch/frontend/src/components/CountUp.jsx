import { useEffect, useRef, useState } from 'react';

/** Anima un número de 0 hasta `value` cuando entra en pantalla. */
export default function CountUp({ value, duration = 1000 }) {
  const target = Number(value) || 0;
  const [display, setDisplay] = useState(0);
  const ref = useRef(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    let frame;
    let cancelled = false;

    function animate() {
      const start = performance.now();
      function step(now) {
        const progress = Math.min((now - start) / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        if (!cancelled) setDisplay(Math.round(target * eased));
        if (progress < 1 && !cancelled) frame = requestAnimationFrame(step);
      }
      frame = requestAnimationFrame(step);
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          animate();
          observer.disconnect();
        }
      },
      { threshold: 0.4 }
    );

    observer.observe(el);
    return () => {
      cancelled = true;
      observer.disconnect();
      cancelAnimationFrame(frame);
    };
  }, [target, duration]);

  return <span ref={ref}>{display.toLocaleString('es-ES')}</span>;
}

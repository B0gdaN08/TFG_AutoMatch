import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import { imageUrl } from '../api';

export default function Lightbox({ images, index = 0, onClose }) {
  const [current, setCurrent] = useState(index);
  const hasMultiple = images.length > 1;

  useEffect(() => {
    function handleKey(e) {
      if (e.key === 'Escape') onClose();
      if (e.key === 'ArrowRight') setCurrent((i) => (i + 1) % images.length);
      if (e.key === 'ArrowLeft') setCurrent((i) => (i - 1 + images.length) % images.length);
    }
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [images.length, onClose]);

  return createPortal(
    <div className="lightbox" onClick={onClose}>
      <button className="lightbox-close" onClick={onClose} aria-label="Cerrar">×</button>
      <img
        className="lightbox-image"
        src={imageUrl(images[current])}
        alt=""
        onClick={(e) => e.stopPropagation()}
      />
      {hasMultiple && (
        <>
          <button
            className="lightbox-nav lightbox-prev"
            onClick={(e) => { e.stopPropagation(); setCurrent((i) => (i - 1 + images.length) % images.length); }}
            aria-label="Foto anterior"
          >
            ‹
          </button>
          <button
            className="lightbox-nav lightbox-next"
            onClick={(e) => { e.stopPropagation(); setCurrent((i) => (i + 1) % images.length); }}
            aria-label="Foto siguiente"
          >
            ›
          </button>
        </>
      )}
    </div>,
    document.body
  );
}

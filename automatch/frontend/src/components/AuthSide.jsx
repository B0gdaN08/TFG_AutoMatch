import { HeartIcon, TagIcon, CompareIcon } from './icons';

const FEATURES = [
  {
    icon: <HeartIcon />,
    title: 'Favoritos',
    text: 'Guarda los coches que te interesan y vuelve a ellos cuando quieras.',
  },
  {
    icon: <TagIcon />,
    title: 'Vende tu coche',
    text: 'Publica tu anuncio con la misma ficha técnica que el catálogo.',
  },
  {
    icon: <CompareIcon />,
    title: 'Compara y decide',
    text: 'Accede al comparador y al recomendador desde cualquier dispositivo.',
  },
];

/** Panel decorativo (estilo hero) que acompaña a los formularios de Login/Registro. */
export default function AuthSide({ eyebrow, title, lead }) {
  return (
    <div className="auth-side" aria-hidden="true">
      <p className="eyebrow eyebrow-light">{eyebrow}</p>
      <p className="auth-side-title">{title}</p>
      <p className="auth-side-lead">{lead}</p>
      <div className="auth-features">
        {FEATURES.map((f) => (
          <div className="auth-feature" key={f.title}>
            <span className="icon-tile">{f.icon}</span>
            <div>
              <h3>{f.title}</h3>
              <p>{f.text}</p>
            </div>
          </div>
        ))}
      </div>
      <div className="hero-road" aria-hidden="true" />
    </div>
  );
}

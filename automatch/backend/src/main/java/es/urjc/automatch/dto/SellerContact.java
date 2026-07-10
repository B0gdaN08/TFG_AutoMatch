package es.urjc.automatch.dto;

/**
 * Datos de contacto del vendedor de un anuncio.
 * Solo se devuelven a usuarios autenticados y para anuncios APPROVED;
 * el email nunca viaja en el JSON publico del coche (CarResponse).
 */
public record SellerContact(String name, String email) {
}

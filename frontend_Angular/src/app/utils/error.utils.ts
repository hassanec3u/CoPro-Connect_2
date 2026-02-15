import { HttpErrorResponse } from '@angular/common/http';

/**
 * Convertit une erreur HTTP en message utilisateur-friendly
 * Ne jamais afficher les détails techniques aux utilisateurs
 */
export function getUserFriendlyErrorMessage(error: any): string {
  // Si c'est une string, vérifier si elle contient des informations techniques
  if (typeof error === 'string') {
    const message = error.trim();
    // Filtrer les messages techniques
    if (message.includes('Http failure') || 
        message.includes('http://') || 
        message.includes('https://') ||
        message.includes('localhost') ||
        message.includes('XMLHttpRequest') ||
        message.match(/^\d{3}\s/)) { // Messages commençant par un code HTTP
      // Extraire le code HTTP si présent
      const httpMatch = message.match(/(\d{3})/);
      if (httpMatch) {
        const status = parseInt(httpMatch[1], 10);
        return getStatusMessage(status);
      }
      // Messages génériques pour erreurs techniques
      if (message.toLowerCase().includes('unauthorized') || message.includes('401')) {
        return 'Identifiants incorrects. Veuillez vérifier votre nom d\'utilisateur et mot de passe.';
      }
      if (message.toLowerCase().includes('forbidden') || message.includes('403')) {
        return 'Accès refusé. Vous n\'avez pas les permissions nécessaires.';
      }
      if (message.includes('404')) {
        return 'Ressource introuvable.';
      }
      if (message.includes('500')) {
        return 'Erreur serveur. Veuillez réessayer plus tard.';
      }
      return 'Une erreur est survenue. Veuillez réessayer.';
    }
    return message;
  }

  // Si c'est une HttpErrorResponse
  if (error instanceof HttpErrorResponse || error?.status) {
    const status = error.status || error.statusCode;
    const errorMessage = error.error?.message || error.message || '';
    
    // Vérifier si le message du serveur est utilisateur-friendly
    if (errorMessage && 
        !errorMessage.includes('Http failure') && 
        !errorMessage.includes('http://') &&
        !errorMessage.includes('localhost')) {
      return errorMessage;
    }
    
    return getStatusMessage(status);
  }

  // Si c'est une Error avec un message
  if (error instanceof Error) {
    const message = error.message;
    // Filtrer les messages techniques
    if (message.includes('Http failure') || 
        message.includes('http://') || 
        message.includes('localhost') ||
        message.includes('XMLHttpRequest')) {
      // Extraire le code HTTP si présent
      const httpMatch = message.match(/(\d{3})/);
      if (httpMatch) {
        const status = parseInt(httpMatch[1], 10);
        return getStatusMessage(status);
      }
      if (message.toLowerCase().includes('unauthorized') || message.includes('401')) {
        return 'Identifiants incorrects. Veuillez vérifier votre nom d\'utilisateur et mot de passe.';
      }
      if (message.includes('404')) {
        return 'Ressource introuvable.';
      }
      if (message.includes('500')) {
        return 'Erreur serveur. Veuillez réessayer plus tard.';
      }
      return 'Une erreur est survenue. Veuillez réessayer.';
    }
    return message;
  }

  // Message par défaut
  return 'Une erreur inattendue est survenue. Veuillez réessayer.';
}

/**
 * Retourne un message utilisateur-friendly selon le code de statut HTTP
 */
function getStatusMessage(status: number): string {
  switch (status) {
    case 400:
      return 'Requête invalide. Veuillez vérifier les informations saisies.';
    case 401:
      return 'Identifiants incorrects. Veuillez vérifier votre nom d\'utilisateur et mot de passe.';
    case 403:
      return 'Accès refusé. Vous n\'avez pas les permissions nécessaires.';
    case 404:
      return 'Ressource introuvable.';
    case 409:
      return 'Conflit : cette ressource existe déjà.';
    case 422:
      return 'Données invalides. Veuillez vérifier les informations saisies.';
    case 500:
      return 'Erreur serveur. Veuillez réessayer plus tard.';
    case 503:
      return 'Service temporairement indisponible. Veuillez réessayer plus tard.';
    default:
      return 'Une erreur est survenue. Veuillez réessayer.';
  }
}

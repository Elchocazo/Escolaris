// =========================================================================
// ESCOLARIS FIREBASE CLOUD MESSAGING (FCM) SERVICE WORKER
// =========================================================================

importScripts('https://www.gstatic.com/firebasejs/10.8.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.8.0/firebase-messaging-compat.js');

try {
  // Inicialización automática cuando está alojado en Firebase Hosting
  importScripts('/__/firebase/init.js');
} catch (e) {
  // Fallback con credenciales oficiales del proyecto
  firebase.initializeApp({
    apiKey: "FIREBASE_API_KEY_PLACEHOLDER",
    authDomain: "escolaris-ab151.firebaseapp.com",
    projectId: "escolaris-ab151",
    storageBucket: "escolaris-ab151.firebasestorage.app",
    messagingSenderId: "1039847291823",
    appId: "1:1039847291823:web:escolaris_pwa_id"
  });
}

const messaging = firebase.messaging();

// Manejador de notificaciones en segundo plano (cuando la pestaña está cerrada o inactiva)
messaging.onBackgroundMessage((payload) => {
  console.log('[firebase-messaging-sw.js] Background message received: ', payload);

  const notificationTitle = payload.notification?.title 
    || payload.data?.title 
    || 'Escolaris - Notificación Escolar';

  const notificationOptions = {
    body: payload.notification?.body || payload.data?.body || payload.data?.message || 'Tienes una nueva actualización escolar.',
    icon: '/icon-192.png',
    badge: '/apple-touch-icon.png',
    vibrate: [200, 100, 200],
    tag: payload.data?.tag || 'escolaris-notification',
    data: {
      url: payload.data?.url || (payload.data?.targetScreen ? `/#${payload.data.targetScreen}` : '/'),
      type: payload.data?.type || 'GENERAL',
      timestamp: Date.now()
    }
  };

  return self.registration.showNotification(notificationTitle, notificationOptions);
});

// Notification click event handler
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const targetUrl = event.notification.data?.url || '/';

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windowClients) => {
      for (let client of windowClients) {
        if (client.url.includes(self.location.origin) && 'focus' in client) {
          return client.focus();
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(targetUrl);
      }
    })
  );
});

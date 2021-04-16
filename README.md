## Getting Started

Code début du TP2

## Folder Structure
- `src`: Les fichiers sources
- `emoji` : Les images des emojis

## FONCTIONALITÉS
-Rentrer les informations de connexion
-Se connecter/déconnecter
-Envoyer des messages
-Voir les connectés
-Couleurs
-Smileys avec alias ( :), :-), ... )
-Entrer pour envoyer/connecter au lieu de devoir appuyer sur le bouton
-Envoyer des messages privées
-Pouvoir voir qu'on a recu des messages (notifications)
-Quelques messages d'erreurs liés aux envois de MP

## AMELIORATIONS POSSIBLES
Les communications serveur-client pourraient être améliorées, car au début il était conçu pour que deux utilisateurs puissent avoir le même pseudo, mais je me suis rendu compte que ça posait problème pour la formulation d'un message privé, car l'utilisateur doit pouvoir "mentionner" l'utilisateur grace à son nom, mais le programme ne pourrait pas savoir de qui il veut parler.
Actuellement, c'est la position dans la liste qui est utilisée comme identifiant, mais même si je n'ai pas eu d'erreurs à cause de ça, je pense qu'il est facilement possible d'obtenir des positions différentes sur plusieurs clients et le serveur, par exemple s'il y a un message de déconnexion et un autre message en même temps. Envoyer leur ID ou pseudo# serait plus sécurisé.

Aussi, je trouve que le code est mal structuré, et passer plus de temps sur la conception aurait certainement été bénéfique pour être mieux organisé et pouvoir améliorer le chat plus facilement par la suite, même si je trouve avoir obtenu un bon résultat assez rapidement.

Des fonctionalités seraient necessaires, comme une commande -help qui afficherait les différents smileys, fermer les onglets, plus de messages d'erreur d'envois qui s'affichent dans le chat...

Enfin, faire un objet pour stocket les champs des onglets de discussion et les méthodes pour les modifier aurait été meilleur.

## BUGS
- Taille de la textArea de composition du message est certaines fois trop petite quand le scrollPane essaie de ne pas mettre la scrollBar

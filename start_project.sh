#!/bin/bash

# Ouvrir un premier onglet pour OpenVPN
tmux new-session -d -s my_project -n vpn "sudo openvpn EpisenCreteil.ovpn"

# Ouvrir un deuxième onglet pour la connexion SSH
tmux new-window -t my_project -n ssh "ssh medicare@172.31.249.24"

# Ouvrir un troisième onglet pour le backend
tmux new-window -t my_project -n backend "cd xmart-city-backend/target && java -jar xmart-zity-backend-1.0-SNAPSHOT-jar-with-dependencies.jar"

# Ouvrir un quatrième onglet pour le frontend
tmux new-window -t my_project -n frontend "cd xmart-frontend/target && java -jar xmart-frontend-1.0-SNAPSHOT-jar-with-dependencies.jar xmart-frontend-1.0-SNAPSHOT.jar"

# Attacher la session tmux pour voir les fenêtres ouvertes
tmux attach-session -t my_project

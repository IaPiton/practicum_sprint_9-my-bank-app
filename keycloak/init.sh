/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin
/opt/keycloak/bin/kcadm.sh add-roles -r bank --uusername service-account-manager-user --cclientid realm-management --rolename manage-realm
/opt/keycloak/bin/kcadm.sh add-roles -r bank --uusername service-account-manager-user --cclientid realm-management --rolename manage-clients
/opt/keycloak/bin/kcadm.sh add-roles -r bank --uusername service-account-manager-user --cclientid realm-management --rolename manage-users
/opt/keycloak/bin/kcadm.sh add-roles -r bank --uusername service-account-manager-user --cclientid realm-management --rolename view-users
/opt/keycloak/bin/kcadm.sh add-roles -r bank --uusername service-account-manager-user --cclientid realm-management --rolename view-clients
/opt/keycloak/bin/kcadm.sh add-roles -r bank --uusername service-account-manager-user --cclientid realm-management --rolename create-client
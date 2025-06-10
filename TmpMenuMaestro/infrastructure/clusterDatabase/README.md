# ClusterDatabase
- The database server in our cluster is already deployed. This folder is just for documenting
- We clear the database before each new deployment.
- But we do not redeploy our database server each time.
    - This would prevent us from having 2 deployments simultainiosly. Because they share the server.
    - Would be using more resources then needed
- If we want to create a new cluster we have to execute the files in this folder once

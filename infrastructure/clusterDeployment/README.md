# ClusterDeployment
- This folder contains all Kubernetes ressources that will be deployed on each successful pipeline run.
-  [deployment.yaml](./deployment.yaml) and [service.yaml](./service.yaml) are generic so if we start a merge request pipeline in the files replace the keys with the corisponding environment (mr or prod)
- The 2 ingress will always be deployed.
    - They are static and Kubernetes detects if no changes to them are made

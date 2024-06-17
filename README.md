# rfbpa

## Cluster setup AKS (quick and dirty test)

* Setup AKS with minimal setu

Make sure to set ingress load balancer health, probably in service annotations

```yaml
service.beta.kubernetes.io/azure-load-balancer-health-probe-request-path: /healthz
```

https://kubernetes.github.io/ingress-nginx/deploy/
```bash
helm upgrade --install ingress-nginx ingress-nginx \
    --repo https://kubernetes.github.io/ingress-nginx \
    --namespace ingress-nginx --create-namespace \
    -f nginx-ingress-values.yaml
```

https://cert-manager.io/docs/installation/helm/
```bash
helm repo add jetstack https://charts.jetstack.io --force-update

helm install \
  cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.15.0 \
  --set crds.enabled=true
```

```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-staging
spec:
  acme:
    email: rohdef+rfbpa@rohdef.dk
    server: https://acme-staging-v02.api.letsencrypt.org/directory
    privateKeySecretRef:
      name: letsencrypt-staging
    solvers:
    - http01:
        ingress:
          ingressClassName: nginx
```

```bash
az aks approuting enable --resource-group rf-bpa-test --name rf-bpa-test-2
```

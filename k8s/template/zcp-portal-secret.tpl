apiVersion: v1
kind: Secret
metadata:
  name: zcp-portal-ui-secret
  namespace: ${namespace}
type: Opaque
data:
  CLIENT_SECRET: NzFhZDFlNDItZDIzOS00YTdkLTk5ZjktZTFlNTBhZDdjYTkz
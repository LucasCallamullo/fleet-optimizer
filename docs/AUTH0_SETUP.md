
<h1>Auth0 Setup (Required for Login)</h1>

  <p>The <code>.env.example</code> file uses placeholders. To run the authentication flow, you need to create a free Auth0 tenant and configure it as follows:</p>

  <ol>
    <li>
      <strong>Create an account</strong> at
      <a href="https://auth0.com/signup">auth0.com/signup</a>.
    </li>
    <li>
      <strong>Create an API</strong> (Applications → APIs → Create API):
      <ul>
        <li>Name: <code>Fleet Optimizer API</code></li>
        <li>Identifier (audience): <code>https://fleetoptimizer.com/api</code></li>
        <li>Signing Algorithm: <code>RS256</code></li>
      </ul>
      <p>
        <em>Optional but recommended:</em> in the same API, go to
        <strong>Settings</strong> and enable <strong>Allow Offline Access</strong>
        so Auth0 returns a <code>refresh_token</code>.
      </p>
    </li>
    <li>
      <strong>Create an Application</strong> (Applications → Applications → Create Application):
      <ul>
        <li>Type: <strong>Regular Web Application</strong></li>
        <li>Note the <strong>Domain</strong>, <strong>Client ID</strong>, and <strong>Client Secret</strong>.</li>
      </ul>
    </li>
    <li>
      <strong>Authorize the Application for your API</strong>
      (Applications → APIs → [your API] → <strong>Application Access</strong>):
      <ul>
        <li>Enable <strong>User-Delegated Access</strong> for your application.</li>
        <li>Select the scopes you need (<code>openid</code>, <code>profile</code>, <code>email</code>, <code>offline_access</code>).</li>
      </ul>
    </li>
    <li>
      <strong>Enable Grant Types</strong>
      (Applications → [your app] → Advanced Settings → Grant Types):
      <ul>
        <li>Check <strong>Password</strong> (for login with email/password).</li>
        <li>Check <strong>Refresh Token</strong> (for silent token renewal).</li>
        <li>Check <strong>Client Credentials</strong> (for the M2M Management API token used during registration).</li>
      </ul>
    </li>
    <li>
      <strong>Create a Post-Login Action</strong>
      (Actions → Library → Create Action → <em>Build from scratch</em>,
      trigger: <strong>Login / Post Login</strong>).
      <p>
        This Action injects custom claims (<code>roles</code>, <code>email</code>)
        into the tokens. <strong>Without it, the Gateway will not be able to
        propagate user roles or email to downstream microservices.</strong>
      </p>
      <pre><code>exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://fo.dev';

  // 1. Try native Auth0 roles assigned via the dashboard
  let roles = event.authorization?.roles || [];

  // 2. Fall back to the role stored in user_metadata at /register time
  if (roles.length === 0 && event.user.user_metadata?.role) {
    roles.push(event.user.user_metadata.role);
  }

  // 3. Default role if none found
  if (roles.length === 0) {
    roles.push('User');
  }

  // 4. Inject custom claims into the tokens
  api.accessToken.setCustomClaim(`${namespace}/roles`, roles);
  api.idToken.setCustomClaim(`${namespace}/roles`, roles);
  api.accessToken.setCustomClaim(`${namespace}/email`, event.user.email);
};</code></pre>
      <p>
        After creating the Action, drag it into the <strong>Login</strong> flow
        and click <strong>Apply</strong> (top right).
      </p>
    </li>
    <li>
      <strong>Copy your credentials</strong> into your <code>.env</code> file:
      <pre><code>AUTH0_DOMAIN=your-tenant.us.auth0.com
AUTH0_CLIENT_ID=your-client-id
AUTH0_CLIENT_SECRET=your-client-secret
AUTH0_AUDIENCE=https://your-tenant.us.auth0.com/api/v2/
AUTH0_LOGIN_AUDIENCE=https://fleetoptimizer.com/api
AUTH0_JWKS_URI=https://your-tenant.us.auth0.com/.well-known/jwks.json</code></pre>
      <p>
        <strong>Note:</strong> <code>AUTH0_AUDIENCE</code> points to the
        Management API (used for M2M), while <code>AUTH0_LOGIN_AUDIENCE</code>
        points to your business API (used for user login).
      </p>
    </li>
  </ol>

  <blockquote>
    <p><strong>Troubleshooting common errors:</strong></p>
    <ul>
      <li>
        <code>Client is not authorized to access resource server</code> →
        re-check step 4 (Application Access).
      </li>
      <li>
        <code>Grant type 'password-realm' not allowed</code> →
        re-check step 5 (Grant Types).
      </li>
      <li>
        <code>Password is too weak</code> → adjust the password policy in
        Authentication → Database → Username-Password-Authentication →
        Authentication Methods.
      </li>
      <li>
        Roles are missing from the JWT → the Post-Login Action (step 6)
        is not deployed or not attached to the Login flow.
      </li>
    </ul>
  </blockquote>
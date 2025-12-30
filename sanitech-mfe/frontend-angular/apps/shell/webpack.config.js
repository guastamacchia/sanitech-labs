const ModuleFederationPlugin = require('@angular-architects/module-federation/webpack').ModuleFederationPlugin;
module.exports = {
  output: { uniqueName: 'shell' },
  plugins: [
    new ModuleFederationPlugin({
      remotes: {
        patient: 'patient@http://localhost:4301/remoteEntry.js',
        doctor:  'doctor@http://localhost:4302/remoteEntry.js',
        admin:   'admin@http://localhost:4303/remoteEntry.js'
      },
      shared: { }
    })
  ]
};

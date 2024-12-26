import EmberRouter from '@ember/routing/router';
import config from 'project/config/environment';

export default class Router extends EmberRouter {
  location = config.locationType;
  rootURL = config.rootURL;
}

Router.map(function () {
  this.route('index', { path: '/' });
  this.route('signup');
  this.route('landing');
  this.route('newdocument');
  this.route('create-document');
  this.route('shared-document');
});

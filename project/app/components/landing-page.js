import Component from '@glimmer/component';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from "@glimmer/tracking";

export default class LandingPageComponent extends Component {
  @service router;
  @service cookies;

  @tracked username = '';

  constructor() {
    super(...arguments);
    this.fetchUsername();
  }

  async fetchUsername() {
    try {
      const response = await fetch('http://localhost:8080/name_saver_war_exploded/get-username', {
        method: 'GET',
        credentials: 'include',
      });

      if (response.ok) {
        const data = await response.json();
        this.username = data.username;
      } else {
        throw new Error('Failed to get username');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  }

  @action
  async handleLogout() {
    try {
      const response = await fetch('http://localhost:8080/name_saver_war_exploded/logout', {
        method: 'POST',
        credentials: 'include',
      });

      if (response.ok) {
        console.log('Logout successful');
        this.router.transitionTo('index');
      } else {
        console.error('Logout failed');
        alert('Logout failed. Please try again.');
      }
    } catch (error) {
      console.error('Error during logout:', error);
      alert('An error occurred during logout.');
    }
  }

  @action
  createNewDocument() {
    this.router.transitionTo('newdocument');
  }

  @action
  viewCreatedDocuments() {
    this.router.transitionTo('create-document');
  }

  @action
  viewSharedDocuments() {
    this.router.transitionTo('shared-document');
  }
}

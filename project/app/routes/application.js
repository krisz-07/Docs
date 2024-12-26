import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class ApplicationRoute extends Route {
  @service cookies;
  @service router;

  async beforeModel() {
    try {
      const username = await this.getUsername();
      if (username) {
        this.router.transitionTo('landing');
      } else {
        this.router.transitionTo('index');
      }
    } catch (error) {
      console.error('Error fetching username:', error);
      this.router.transitionTo('index');
    }
  }

  async getUsername() {
    try {
      const response = await fetch('http://localhost:8080/name_saver_war_exploded/get-username', {
        method: 'GET',
        credentials: 'include',
      });

      if (response.ok) {
        const data = await response.json();
        return data.username || null;
      } else {
        throw new Error('Failed to get username');
      }
    } catch (error) {
      console.error('Error fetching username:', error);
      return null;
    }
  }
}

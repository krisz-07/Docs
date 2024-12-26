import Component from '@glimmer/component';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';

export default class LoginFormComponent extends Component {
  @tracked username = '';
  @tracked password = '';
  @service router;

  @action
  updateUsername(event) {
    this.username = event.target.value;
  }

  @action
  updatePassword(event) {
    this.password = event.target.value;
  }

  @action
  async handleLogin(event) {
    event.preventDefault();

    const login = `username=${this.username}&password=${this.password}`;

    try {
      const response = await fetch('http://localhost:8080/name_saver_war_exploded/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: login,
        credentials: 'include',
      });
      const data = await response.text();
      if (data.trim()==="success") {
        this.router.transitionTo('landing');
      }else {
        alert(data);
      }
    } catch (error) {
      console.error('Error during login or fetching username:', error);
      alert('An error occurred. Please try again.');
    }
  }
}

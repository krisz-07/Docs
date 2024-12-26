import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import fetch from 'fetch';

export default class SignupFormComponent extends Component {
  @tracked username = '';
  @tracked password = '';
  @tracked errorMessage = '';

  @action
  updateUsername(event) {
    this.username = event.target.value;
  }

  @action
  updatePassword(event) {
    this.password = event.target.value;
  }

  @action
  async handleSignup(event) {
    event.preventDefault();

    this.errorMessage = '';


    if (!this.username || !this.password) {
      this.errorMessage = 'Username and password are required!';
      console.error(this.errorMessage);
      return;
    }

    this.sendSignupData(this.username, this.password);
  }

  async sendSignupData(username, password) {
    try {
      const response = await fetch('http://localhost:8080/name_saver_war_exploded/signup', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `username=${username}&password=${password}`,
      });

      const data = await response.text();
      if (data=== 'Success') {
        alert('Signup successful!');
      } else {
        alert(data);
      }
    } catch (error) {
      console.error('Error:', error);
      alert('An error occurred while processing your request. Please try again.');
    }
  }
}

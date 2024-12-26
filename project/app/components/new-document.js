import Component from '@glimmer/component';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';

export default class NewDocumentComponent extends Component {
  @tracked title = '';
  @tracked content = '';
  @tracked username = '';
  @service router;

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
        this.username = data.username || 'Guest';
      } else {
        throw new Error('Failed to get username');
      }
    } catch (error) {
      alert('An error occurred while retrieving the username.');
    }
  }

  @action
  updateTitle(event) {
    this.title = event.target.value;
  }

  @action
  updateContent(event) {
    this.content = event.target.value;
  }

  @action
  async submitForm(event) {
    event.preventDefault();

    const documentData = {
      title: this.title,
      content: this.content,
      username: this.username,
    };

    try {

      const formData = new URLSearchParams(documentData).toString();

      const response = await fetch('http://localhost:8080/name_saver_war_exploded/createdocumentfile', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData,
      });
      const data = await response.text();

      if (data.trim() ==="Success") {
        alert('Document created successfully!');
        this.router.transitionTo('create-document');
      } else {
        alert(data);
      }
    } catch (error) {
      alert('An error occurred while creating the document.');
    }
  }
}

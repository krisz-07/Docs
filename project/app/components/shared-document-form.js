import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';

export default class SharedDocumentsComponent extends Component {
  @tracked sharedDocuments = [];
  @tracked selectedSharedDocument = null;
  @tracked username = '';
  @service cookies;
  websocket = null;
  @tracked cursorPosition =0;
  @tracked isCapsLockOn = true;
  @tracked isPasting;
  @tracked keys = ['Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'];
  @tracked keys1 = ['A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'];
  @tracked keys2 = ['Z', 'X', 'C', 'V', 'B', 'N', 'M'];

  constructor() {
    super(...arguments);
    this.getUsername().then(() => {
      this.loadSharedDocuments();
      this.setupWebSocket();
    });
  }
  get displayedKeys() {
    return {
      row1: this.keys,
      row2: this.keys1,
      row3: this.keys2
    };
  }
  async getUsername() {
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
      console.error('Error fetching username:', error);
      alert('An error occurred while retrieving the username.');
    }
  }

  @action
  async loadSharedDocuments() {
    try {
      const response = await fetch(`http://localhost:8080/name_saver_war_exploded/shared-documentsfile/${this.username}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch shared documents');
      }

      const data = await response.json();
      this.sharedDocuments = Array.isArray(data) ? data : [];
    } catch (error) {
      console.error('Error fetching shared documents:', error);
      alert('An error occurred while fetching shared documents.');
    }
  }

  get editableDocuments() {
    return this.sharedDocuments.filter((doc) => doc.accessType === 'edit');
  }

  get viewOnlyDocuments() {
    return this.sharedDocuments.filter((doc) => doc.accessType === 'view');
  }

  @action
  async selectSharedDocument(document) {
    if (
      this.selectedSharedDocument &&
      this.selectedSharedDocument.title === document.title &&
      this.selectedSharedDocument.owner === document.owner
    ) {
      console.log('Document already selected:', document.title);
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/name_saver_war_exploded/documentfile/content/${document.owner}/${document.title}`,
        {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' },
        }
      );

      if (response.ok) {
        const content = await response.text();
        this.selectedSharedDocument = { ...document, content };

      } else {
        throw new Error('Failed to load document content');
      }
    } catch (error) {
      console.error('Error fetching document content:', error);
      alert('An error occurred while fetching the document content.');
    }
  }

  changeKeyColor(keyId) {
    const keyElement = document.getElementById(keyId);
    if (keyElement) {
      keyElement.style.backgroundColor = 'red';
      setTimeout(() => {
        keyElement.style.backgroundColor = '#007bff';
      }, 3000);
    }
  }


  findContentChanges(currentContent, previousContent) {
    let startIndex = 0;
    let endIndexPrevious = previousContent.length;
    let endIndexCurrent = currentContent.length;

    while (
      startIndex < endIndexPrevious &&
      startIndex < endIndexCurrent &&
      currentContent[startIndex] === previousContent[startIndex]
      ) {
      startIndex++;
    }
    while (
      endIndexPrevious > startIndex &&
      endIndexCurrent > startIndex &&
      currentContent[endIndexCurrent - 1] === previousContent[endIndexPrevious - 1]
      ) {
      endIndexPrevious--;
      endIndexCurrent--;
    }

    const changedContent = currentContent.slice(startIndex, endIndexCurrent);

    if (this.websocket && this.selectedSharedDocument.title) {
      const messageObj = {
        username: this.username,
        owner: this.username,
        title: this.selectedSharedDocument.title,
        startIndex: startIndex,
        endIndex: endIndexPrevious,
        changedContent: changedContent,
      };
      if(changedContent.length === 1 && this.isPasting === false){
        this.changeButtonColor(changedContent);
      }
      const message = JSON.stringify(messageObj);
      this.websocket.send(message);
    }
  }
  @action
  changeButtonColor(key){
    const keyId = 'key-'+key;
    console.log('keyid to change color',keyId);
    const keyElement = document.getElementById(keyId);
    if (keyElement) {
      keyElement.style.backgroundColor = 'green';
      setTimeout(() => {
        keyElement.style.backgroundColor = '#007bff';
      }, 200);
    }

  }
  @action
  togglecase() {
    this.keys = this.keys.map((key) =>
      this.isCapsLockOn ? key.toLowerCase() : key.toUpperCase()
    );

    this.keys1 = this.keys1.map((key) =>
      this.isCapsLockOn ? key.toLowerCase() : key.toUpperCase()
    );

    this.keys2 = this.keys2.map((key) =>
      this.isCapsLockOn ? key.toLowerCase() : key.toUpperCase()
    );
    this.isCapsLockOn = !this.isCapsLockOn;
  }
  @action
  insertCharacter(character) {
    if (this.selectedSharedDocument) {
      const previousContent = this.selectedSharedDocument.content;
      const content = this.selectedSharedDocument.content;
      let cursorPos = this.cursorPosition;
      character = this.isCapsLockOn ? character.toUpperCase() : character.toLowerCase();
      const updatedContent = content.slice(0, cursorPos) + character + content.slice(cursorPos);
      this.selectedSharedDocument = {
        ...this.selectedSharedDocument,
        content: updatedContent,
      };

      this.cursorPosition += character.length;
      const id = 'key-'+character;
      const messageObj ={
        username: this.username,
        owner: this.selectedSharedDocument.owner,
        title:this.selectedSharedDocument.title,
        id:id,
      };
      const message = JSON.stringify(messageObj);
      this.websocket.send(message);
      this.findContentChanges(updatedContent, previousContent);
    }
  }

  @action
  updateSharedContent(event) {
    if (this.selectedSharedDocument) {
      const currentContent = event.target.value;
      const previousContent = this.selectedSharedDocument.content;
      this.isPasting = event.inputType === 'insertFromPaste';
      console.log(this.isPasting);
      this.findContentChanges(currentContent, previousContent);
      this.selectedSharedDocument.content = currentContent;
    }
  }


  @action
  updateCursorPosition(event) {
    this.cursorPosition = event.target.selectionStart;
    if (this.websocket && this.selectedSharedDocument) {
      const messageObj = {
        username: this.username,
        owner: this.selectedSharedDocument.owner,
        title: this.selectedSharedDocument.title,
        cursorPosition: this.cursorPosition,
      };

      const message = JSON.stringify(messageObj);
      console.log('message for cursor',message);
      this.websocket.send(message);
    }
  }

  @action
  async saveSharedDocument(event) {
    event.preventDefault();

    if (!this.selectedSharedDocument) {
      alert('No document selected for saving.');
      return;
    }

    const documentData = {
      title: this.selectedSharedDocument.title,
      content: this.selectedSharedDocument.content,
      username: this.selectedSharedDocument.owner,
    };

    try {
      const response = await fetch('http://localhost:8080/name_saver_war_exploded/updatefile', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(documentData).toString(),
      });

      if (response.ok) {
        alert('Shared document updated successfully!');
      } else {
        throw new Error('Failed to update shared document');
      }
    } catch (error) {
      console.error('Error saving shared document:', error);
      alert('An error occurred while updating the shared document.');
    }
  }

  @action
  downloadDocument() {
    if (!this.selectedSharedDocument) {
      alert('No document selected.');
      return;
    }

    const { title, content } = this.selectedSharedDocument;
    const blob = new Blob([content], { type: 'text/plain' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `${title}.txt`;
    link.click();
    URL.revokeObjectURL(link.href);
  }

  setupWebSocket() {
    this.websocket = new WebSocket('ws://localhost:8080/name_saver_war_exploded/document-websocket');

    this.websocket.onopen = () => {
      console.log('WebSocket connection established');
    };

    this.websocket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log('Received message:', data);

        if (data.cursorPosition !== undefined && data.cursorPosition !== null && data.username !== this.username) {
          const contentElement = document.querySelector('#document-content');
          if (contentElement) {
            const existingMarker = document.querySelector('.highlight-marker');
            if (existingMarker) {
              existingMarker.remove();
            }

            const currentContent = this.selectedSharedDocument ? this.selectedSharedDocument.content : '';
            const fontSize = parseInt(window.getComputedStyle(contentElement).fontSize, 10);
            const lineHeight = parseInt(window.getComputedStyle(contentElement).lineHeight, 10) || fontSize * 1.2;
            const charWidth = fontSize * 0.6;
            const textBeforeCursor = currentContent.slice(0, data.cursorPosition);
            const lines = textBeforeCursor.split('\n');

            const rowIndex = lines.length - 1;
            const columnIndex = lines[rowIndex].length;

            const top = rowIndex * lineHeight + 12;
            const left = columnIndex * charWidth + 12;

            const marker = document.createElement('div');
            marker.className = 'highlight-marker';
            marker.style.position = 'absolute';
            marker.style.height = `${lineHeight}px`;
            marker.style.width = '2px';
            marker.style.backgroundColor = 'blue';
            marker.style.left = `${left}px`;
            marker.style.top = `${top}px`;


            const parentContainer = contentElement.parentNode;
            if (parentContainer) {
              parentContainer.style.position = 'relative';
              parentContainer.appendChild(marker);
            }
          }
        }else if(data.id !== undefined && data.id !== null && data.username !== this.username){
          const id = data.id;
          this.changeKeyColor(id);
        }
        else {
          const { username, owner, title, startIndex, endIndex, changedContent } = data;

          if (
            this.selectedSharedDocument &&
            this.selectedSharedDocument.title === title &&
            this.selectedSharedDocument.owner === owner &&
            this.username !== username
          ) {
            const start = parseInt(startIndex, 10);
            const end = parseInt(endIndex, 10);

            let content =
              this.selectedSharedDocument.content.slice(0, start) +
              changedContent +
              this.selectedSharedDocument.content.slice(end);

            this.selectedSharedDocument = {
              ...this.selectedSharedDocument,
              content: content,
            };

            console.log('Updated shared document content dynamically:', this.selectedSharedDocument.content);
          } else {
            console.log('Message received for a different document or by the same user.');
          }
        }
      } catch (error) {
        console.error('Failed to parse WebSocket message:', error);
      }
    };
    this.websocket.onclose = () => {
      console.log('WebSocket connection closed');
    };

    this.websocket.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  }

}

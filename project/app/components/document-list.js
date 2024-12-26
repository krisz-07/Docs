import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
export default class DocumentListComponent extends Component {
  @tracked documents = [];
  @tracked selectedDocument = null;
  @tracked isSharePopupVisible = false;
  @tracked usernameToShare = '';
  @tracked accessType = '';
  @tracked username = '';
  websocket = null;
  @tracked cursorPosition = 0;
  @tracked isCapsLockOn = false;
  @tracked isPasting = true;
  @tracked keys = ['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'];
  @tracked keys1 = ['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'];
  @tracked keys2 = ['z', 'x', 'c', 'v', 'b', 'n', 'm'];

  constructor() {
    super(...arguments);
    this.fetchUsername().then(() => {
      this.loadDocuments();
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
  async loadDocuments() {
    try {
      let response = await fetch(`http://localhost:8080/name_saver_war_exploded/documentfile/${this.username}`, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
      });
      let data = await response.json();
      this.documents = Array.isArray(data) ? data : [];
    } catch (error) {
      alert('An error occurred while fetching documents.');
    }
  }

  @action
  async selectDocument(document) {
    try {
      const response = await fetch(`http://localhost:8080/name_saver_war_exploded/documentfile/content/${this.username}/${document.title}`, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
      });
      if (response.ok) {
        const data = await response.text();
        this.selectedDocument = {title: document.title, content: data};
      } else {
        throw new Error('Failed to load document content');
      }
    } catch (error) {
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
    if (this.websocket && this.selectedDocument.title) {
      const messageObj = {
        username: this.username,
        owner: this.username,
        title: this.selectedDocument.title,
        startIndex: startIndex,
        endIndex: endIndexPrevious,
        changedContent: changedContent,
      };

      const message = JSON.stringify(messageObj);
      this.websocket.send(message);
    }
    if (changedContent.length === 1 && this.isPasting === false) {
      this.changeButtonColor(changedContent);
    }
    return changedContent;
  }

  @action
  togglecase() {
    this.isCapsLockOn = !this.isCapsLockOn;
    this.updateKeyCase();
  }

  updateKeyCase() {
    this.keys = this.keys.map((key) =>
      this.isCapsLockOn ? key.toUpperCase() : key.toLowerCase()
    );

    this.keys1 = this.keys1.map((key) =>
      this.isCapsLockOn ? key.toUpperCase() : key.toLowerCase()
    );

    this.keys2 = this.keys2.map((key) =>
      this.isCapsLockOn ? key.toUpperCase() : key.toLowerCase()
    );
  }

  @action
  insertCharacter(character) {

    if (this.selectedDocument) {
      const previousContent = this.selectedDocument.content;
      const content = this.selectedDocument.content;
      let cursorPos = this.cursorPosition;
      character = this.isCapsLockOn ? character.toUpperCase() : character.toLowerCase();
      const updatedContent = content.slice(0, cursorPos) + character + content.slice(cursorPos);
      this.selectedDocument = {
        ...this.selectedDocument,
        content: updatedContent,
      };
      this.cursorPosition += character.length;
      const id = 'key-' + character;
      const messageObj = {
        username: this.username,
        owner: this.username,
        title: this.selectedDocument.title,
        id: id,
      };
      const message = JSON.stringify(messageObj);
      this.websocket.send(message);
    }
  }

  @action
  changeButtonColor(key) {
    const keyId = 'key-' + key;
    console.log('keyid to change color', keyId);
    const keyElement = document.getElementById(keyId);
    if (keyElement) {
      keyElement.style.backgroundColor = 'green';
      setTimeout(() => {
        keyElement.style.backgroundColor = '#007bff';
      }, 300);
    }

  }

  @action
  updateContent(event) {
    if (this.selectedDocument) {
      const currentContent = event.target.value;
      const previousContent = this.selectedDocument.content;
      this.isPasting = event.inputType === 'insertFromPaste';
      this.findContentChanges(currentContent, previousContent);
      this.selectedDocument.content = currentContent;
    }
  }


  @action
  updateCursorPosition(event) {
    this.cursorPosition = event.target.selectionStart;
    if (this.websocket && this.selectedDocument && this.selectedDocument.title) {
      const messageObj = {
        username: this.username,
        owner: this.username,
        title: this.selectedDocument.title,
        cursorPosition: this.cursorPosition,
      };

      const message = JSON.stringify(messageObj);
      this.websocket.send(message);
    }
  }

  @action
  async saveDocument(event) {
    event.preventDefault();

    const documentData = {
      title: this.selectedDocument.title,
      content: this.selectedDocument.content,
      username: this.username,
    };

    try {
      const formData = new URLSearchParams(documentData).toString();

      const response = await fetch('http://localhost:8080/name_saver_war_exploded/updatefile', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: formData,
      });
      const data = await response.text();
      if (data.trim() === "success") {
        alert('Document updated successfully!');
        this.loadDocuments();
      } else {
        alert('Failed to update document. Please try again.');
      }
    } catch (error) {
      console.error('Error updating document:', error);
      alert('An error occurred while updating the document.');
    }
  }

  @action
  toggleSharePopup() {
    this.isSharePopupVisible = !this.isSharePopupVisible;

    // If the popup is being shown, generate the QR code
    if (this.isSharePopupVisible) {
      this.generateQRCode();
    }
  }


  @action
  updateUsername(event) {
    this.usernameToShare = event.target.value;
    console.log('Updated Username to Share:', this.usernameToShare);
  }

  @action
  updateAccessType(event) {
    this.accessType = event.target.value;
    console.log('Updated Access Type:', this.accessType);
  }

  @action
  async handleShareDocument(event) {
    event.preventDefault();

    if (!this.usernameToShare || !this.accessType) {
      alert('Please provide both a username and access type.');
      return;
    }

    const shareData = {
      title: this.selectedDocument.title,
      usernameToShare: this.usernameToShare,
      accessType: this.accessType,
      currentUsername: this.username,
    };
    console.log('Share Data:', shareData);

    try {
      const formData = new URLSearchParams(shareData).toString();

      const response = await fetch('http://localhost:8080/name_saver_war_exploded/sharefile', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: formData,
      });

      const data = await response.text();
      if (data.trim() === 'Document shared successfully.' || data.trim() === 'Document access updated successfully.') {
        alert(data);
        this.toggleSharePopup();
      } else {
        alert(data);
      }
    } catch (error) {
      alert('An error occurred while sharing the document.');
    }
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

        const title = this.selectedDocument.title;

        if (
          data.cursorPosition !== undefined &&
          data.cursorPosition !== null &&
          data.title === title &&
          data.username !== this.username
        ) {
          const contentElement = document.querySelector(`#document-content`);
          if (contentElement) {
            const parentContainer = contentElement.parentNode;

            if (parentContainer) {
              parentContainer.style.position = 'relative';


              const existingMarker = parentContainer.querySelector('.highlight-marker');
              if (existingMarker) {
                existingMarker.remove();
              }

              const currentContent = this.selectedDocument ? this.selectedDocument.content : '';
              const fontSize = parseInt(window.getComputedStyle(contentElement).fontSize, 10);
              const lineHeight = parseInt(window.getComputedStyle(contentElement).lineHeight, 10) || fontSize * 1.2;
              const charWidth = fontSize * 0.6;

              const textBeforeCursor = currentContent.slice(0, data.cursorPosition);
              const lines = textBeforeCursor.split('\n');
              const rowIndex = lines.length - 1;
              const columnIndex = lines[rowIndex].length;


              const top = (rowIndex * lineHeight + 12) - contentElement.scrollTop;
              const left = columnIndex * charWidth + 12;


              const marker = document.createElement('div');
              marker.className = 'highlight-marker';
              marker.style.position = 'absolute';
              marker.style.height = `${lineHeight}px`;
              marker.style.width = '2px';
              marker.style.backgroundColor = 'blue';
              marker.style.left = `${left}px`;
              marker.style.top = `${top}px`;

              parentContainer.appendChild(marker);
            }
          }
        } else if (data.id !== undefined && data.id !== null && data.username !== this.username) {
          const id = data.id;
          this.changeKeyColor(id);
        } else {
          const {username, owner, title, startIndex, endIndex, changedContent} = data;

          if (
            this.selectedDocument &&
            this.selectedDocument.title === title &&
            this.username !== username
          ) {
            const start = parseInt(startIndex, 10);
            const end = parseInt(endIndex, 10);

            let content =
              this.selectedDocument.content.slice(0, start) +
              changedContent +
              this.selectedDocument.content.slice(end);

            this.selectedDocument = {
              ...this.selectedDocument,
              content: content,
            };

            console.log('Updated shared document content dynamically:', this.selectedDocument.content);
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

  @action
  generateQRCode() {
    const qrContainer = document.querySelector("#qr-code-container");

    // URL to display the document content
    const qrText = `http://10.94.75.10:8080/name_saver_war_exploded/documentfile/content/${this.username}/${this.selectedDocument.title}`;

    // Generate the QR code
    new QRCode(qrContainer, {
      text: qrText,
      width: 200,
      height: 200,
    });

    // Fetch the document content after the QR code scan
    this.fetchDocumentContent(qrText);
  }

  fetchDocumentContent(url) {
    // Fetch the document content from the servlet (for viewing)
    fetch(url)
      .then(response => response.text())
      .then(content => {
        // Show the content to the user (this could be in a modal or a page)
        this.showDocumentContent(content);

        // Wait for 10 seconds before initiating the download
        setTimeout(() => {
          this.triggerDownload(url); // Trigger download after 10 seconds
        }, 10000); // 10000ms = 10 seconds
      })
      .catch(error => {
        console.error("Error fetching document content:", error);
      });
  }

  showDocumentContent(content) {
    // Display the content in the page (example: in a modal or a div)
    const contentContainer = document.querySelector("#content-container");
    contentContainer.innerHTML = content; // Add the document content for viewing
  }

  triggerDownload(url) {
    // Trigger the download by adding the download=true parameter to the URL
    const downloadUrl = `${url}?download=true`;

    // Trigger the download
    fetch(downloadUrl)
      .then(response => response.blob())
      .then(blob => {
        // Create a link to trigger the download
        const link = document.createElement("a");
        const downloadUrl = window.URL.createObjectURL(blob);
        link.href = downloadUrl;
        link.download = `${this.selectedDocument.title}.txt`; // Download the file with the original name
        link.click();

        // Clean up the object URL
        window.URL.revokeObjectURL(downloadUrl);
      })
      .catch(error => {
        console.error("Error triggering the download:", error);
      });
  }


}

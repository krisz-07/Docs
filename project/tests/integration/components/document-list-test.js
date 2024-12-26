import { module, test } from 'qunit';
import { setupRenderingTest } from 'project/tests/helpers';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | document-list', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<DocumentList />`);

    assert.dom().hasText('');

    // Template block usage:
    await render(hbs`
      <DocumentList>
        template block text
      </DocumentList>
    `);

    assert.dom().hasText('template block text');
  });
});

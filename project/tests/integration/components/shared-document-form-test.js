import { module, test } from 'qunit';
import { setupRenderingTest } from 'project/tests/helpers';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | shared-document-form', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<SharedDocumentForm />`);

    assert.dom().hasText('');

    // Template block usage:
    await render(hbs`
      <SharedDocumentForm>
        template block text
      </SharedDocumentForm>
    `);

    assert.dom().hasText('template block text');
  });
});

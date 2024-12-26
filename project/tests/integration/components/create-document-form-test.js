import { module, test } from 'qunit';
import { setupRenderingTest } from 'project/tests/helpers';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | create-document-form', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<CreateDocumentForm />`);

    assert.dom().hasText('');

    // Template block usage:
    await render(hbs`
      <CreateDocumentForm>
        template block text
      </CreateDocumentForm>
    `);

    assert.dom().hasText('template block text');
  });
});

import * as R from 'ramda';
import * as glob from 'glob';
import * as fs from 'fs';
import * as path from 'path';
import * as mkdirp from 'mkdirp';
import * as jsyaml from 'js-yaml';
import * as handlebars from 'handlebars';
import { promisify } from 'util';

const baseDir = __dirname;
const distDir = path.join(__dirname, 'dist');
const globAsync = promisify(glob);

const readData = async (): Promise<{}> => {
    return await R.reduce(
        async (acc, filePath) => {
            const contents = await fs.promises.readFile(filePath, 'utf-8');
            const data = jsyaml.safeLoad(contents) as object;
            return { ...(await acc), ...data };
        },
        {},
        await globAsync(path.join(baseDir, 'src/data/*.yaml'))
    );
};

const render = async () => {
    const hbs = handlebars.create();

    // read yaml data
    const data = await readData();

    // register partials
    await R.forEach(async (filePath) => {
        const fileName = path.basename(filePath, '.html');
        hbs.registerPartial(fileName, await fs.promises.readFile(filePath, 'utf-8'));
    }, await globAsync(path.join(baseDir, 'src/partials/*.html')));

    await fs.promises.mkdir(distDir, { recursive: true })

    // render handlebars templates
    await R.forEach(async (filePath) => {
        const contents = await fs.promises.readFile(filePath, 'utf-8');
        const rendered = hbs.compile(contents)(data);

        const distFilePath = path.join(distDir, path.relative('src', filePath));
        await mkdirp(path.dirname(distFilePath));
        await fs.promises.writeFile(distFilePath, rendered, 'utf-8');
    }, await globAsync(path.join(baseDir, 'src/*.md')));

    await fs.promises.copyFile(path.join(baseDir, 'src/index.html'), path.join(distDir, 'index.html'));
};

render();
